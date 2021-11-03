package com.penta.edge.process;

import com.penta.edge.constant.EdgeInfo;
import com.penta.edge.constant.EdgeNode;
import com.penta.edge.constant.Sender;
import com.penta.edge.domain.Hash;
import com.penta.edge.domain.MetaData;
import com.penta.edge.service.FileManager;
import com.penta.edge.service.HashService;
import com.penta.edge.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeProcess {

    private final FileManager fileManager;
    private final MetaDataService metaDataService;
    private final HashService hashService;
    private final EdgeInfo edgeInfo;
    private final RestTemplate restTemplate;

    /*
     * 파일저장 & MetaData 저장 및 HashTable 생성 프로세스
     * */
    @Transactional
    @SneakyThrows
    public void saveMetaHashFromVehicle(
            MultipartFile file, X509Certificate certificate,
            LocalDateTime receivingTime, MetaData metaData) {

        // Public Key to Hex to hash(sha256)
        String pubKeyEncoded = fileManager.getHash(fileManager.getHex(certificate.getPublicKey().getEncoded()));

        // 인증서가 저장될 경로(파일명 포함)
        Path certPath = fileManager.getSavingVehicleCertPath(pubKeyEncoded + ".crt");
        // 인증서 저장 위치 초기화
        metaData.setCert(certPath.toString());
        metaDataService.save(metaData);
        // (3) HashTable 저장

        Hash hash = Hash.builder()
                .sourceID(Sender.VEHICLE.getValue()+pubKeyEncoded)           // 데이터 파일 송신자
                .dataID(metaData.getDataID())                                // 데이터 파일의 해시값
                .destinationID(Sender.NODE.getValue() + edgeInfo.getName())  // 데이터 파일 수신자
                .timestamp(receivingTime)                                    // 수신 시간
                .build();
        hashService.save(hash);

        // (4) 데이터 파일 저장
        String filePath = fileManager.saveFileFromVehicle(file, metaData.getDataID());

        // (5) 인증서 저장
        // 인증서가 저장될 폴더 확인(or 생성)
        fileManager.makeAllDiretories(fileManager.getSavingVehicleCertPath());
        Path certfilePath = Files.write(certPath, getCertString(certificate).getBytes());  // 인증서 저장

        // TODO :: (임시)파일 받자마자 edge로 전송하는 프로세스
        Random random = new Random();
        EdgeNode edgeNode;
        while (true) {
            EdgeNode edge = EdgeNode.values()[random.nextInt(4)];
            if (!edge.getIP().equals(edgeInfo.getIP())) {
                edgeNode = edge;
                break;
            }
        }

        sendToEdge(edgeNode, filePath, certfilePath.toString(), metaData, hash);

    }


    @Transactional
    @SneakyThrows
    public void saveMetaHashFromEdge(LocalDateTime receivingTime, String uuid, MultipartFile datafile, MultipartFile vehicleCertFile, MetaData metaData) {

        Hash hash = Hash.builder()
                .sourceID(Sender.NODE.getValue()+uuid)                       // 데이터 파일 송신자
                .dataID(metaData.getDataID())                                // 데이터 파일의 해시값
                .destinationID(Sender.NODE.getValue() + edgeInfo.getName())  // 데이터 파일 수신자
                .timestamp(receivingTime)                                    // 수신 시간
                .build();

        hashService.save(hash);
        metaDataService.save(metaData);

        // (4) 데이터 파일 저장
        fileManager.saveFileFromVehicle(datafile, metaData.getDataID());

        // (5) 인증서 저장
        // 인증서가 저장될 폴더 확인(or 생성)
        fileManager.makeAllDiretories(fileManager.getSavingVehicleCertPath());
        // TODO :: 모든 엣지의 디렉토리구조가 동일하다는 가정(동일하지 않을 경우 수정 필요. metadata의 cert, directory가 변경될 수 있음)
        Files.copy(vehicleCertFile.getInputStream(), Paths.get(metaData.getCert()), StandardCopyOption.REPLACE_EXISTING);

    }


    @SneakyThrows
    private String getCertString(X509Certificate x509Certificate) {
        Base64.Encoder encoder = Base64.getEncoder();
        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";
        byte[] derCert = x509Certificate.getEncoded();

        String pemCertPre = new String(encoder.encode(derCert));
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }


    @SneakyThrows
    private void sendToEdge(EdgeNode edge, String dataFilePath, String certFilePath, MetaData metaData, Hash hash) {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = convertObjectToMultiValueMap(metaData, hash);
        body.add("uuid", edgeInfo.getName());
        body.add("datafile", new FileSystemResource(dataFilePath));
        body.add("certfile", new FileSystemResource(certFilePath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        ResponseEntity response = restTemplate.postForEntity("https://" + edge.getIP() + ":8443/api/edge/upload/edge/", requestEntity, String.class);

        log.info("Edge IP {} :: 전송 성공 :: {} ", edge.getIP(), response);

    }


    private MultiValueMap<String, Object> convertObjectToMultiValueMap(Object... objects) {

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        for (Object obj : objects) {
            Field[] fields = obj.getClass().getDeclaredFields();
            String name = obj.getClass().getSimpleName().toLowerCase();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                try {
                    Object o = field.get(obj);
                    if (o instanceof LocalDateTime) {
                        LocalDateTime dateTime = (LocalDateTime) o;
                        String format = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        map.add(name + "." + fieldName, format);
                    } else {
                        map.add(name + "." + fieldName, o == null ? null : o.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        return map;

    }


}
