package com.penta.edge.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.edge.configuration.CertificateProperties;
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
import org.springframework.core.io.ByteArrayResource;
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

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
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

    private final CertificateProperties certificateProperties;


    /*
     * (Device -> Edge) ???????????? & MetaData ?????? ??? HashTable ?????? ????????????
     * */
    @Transactional
    @SneakyThrows
    public EdgeNode[] saveMetaHashFromVehicle(
            MultipartFile file, X509Certificate certificate,
            LocalDateTime receivingTime, MetaData metaData) {

        // Public Key to Hex to hash(sha256)
        String pubKeyEncoded = fileManager.getHash(fileManager.getHex(certificate.getPublicKey().getEncoded()));

        // ???????????? ????????? ??????(????????? ??????)
        Path certPath = fileManager.getSavingVehicleCertPath(pubKeyEncoded + ".crt");
        // ????????? ?????? ?????? ?????????
        metaData.setCert(certPath.toString());
        metaDataService.save(metaData);
        log.info("--------- METADATA ?????? ??????. Exception ?????? ??? ROLLBACK ---------");
        // (3) HashTable ??????

        Hash hash = Hash.builder()
                .sourceId(Sender.VEHICLE.getValue() + pubKeyEncoded)           // ????????? ?????? ?????????
                .dataId(metaData.getDataId())                                // ????????? ????????? ?????????
                .destinationId(Sender.NODE.getValue() + edgeInfo.getUuid())  // ????????? ?????? ?????????
                .timestamp(receivingTime)                                    // ?????? ??????
                .build();
        hashService.save(hash);
        log.info("--------- HASHTABLE ?????? ??????. Exception ?????? ??? ROLLBACK ---------");

        // (4) ????????? ?????? ??????
        String filePath = fileManager.saveFileFromVehicle(file, metaData.getDataId());

        // (5) ????????? ??????
        // ???????????? ????????? ?????? ??????(or ??????)
        fileManager.makeAllDiretories(fileManager.getSavingVehicleCertPath());
        Path certfilePath = Files.write(certPath, getCertString(certificate).getBytes());  // ????????? ??????


        Random random = new Random();
        EdgeNode[] edgeNode = new EdgeNode[2];
        int idx = 0;
        while (idx <= 1) {
            EdgeNode edge = EdgeNode.values()[random.nextInt(10)];
            if (!edge.getIP().equals(edgeInfo.getIP())) {
                if (idx == 0 || (idx == 1 && !edgeNode[0].getIP().equals(edge.getIP()))) {
                    edgeNode[idx] = edge;
                    idx++;
                }
            }
        }


        // MEMO :: device?????? ???????????? ???????????? central(Auth)??? ??????
        sendFilesToCentral(filePath, certfilePath.toString(), metaData, hash);

        // todo : keti????????? ?????? ??????
        sendToEdge(edgeNode[0], filePath, certfilePath.toString(), metaData, hash);
        sendToEdge(edgeNode[1], filePath, certfilePath.toString(), metaData, hash);

        return edgeNode;



    }

    /*
     * KETI SOCKET(16300) ??????
     * */
    public void sendToEdge(String dataid, EdgeNode edge) {

        InputStream input = null;

        String hostname = "127.0.0.1";
        int port = 16300;
        int timeout = 10000;

        try(Socket socket = new Socket(hostname, port);
            OutputStream output = socket.getOutputStream();) {
            socket.setSoTimeout(timeout);
            log.info("READ TIMEOUT ?????? :: {}", timeout);

            //{[{REQ::ID::REQ_CODE::REQ_DATA}]}
            String reqString = "{[{REQ::" + edge.getIP() + "::007::" + dataid + "}]}";
            byte[] data = reqString.getBytes(StandardCharsets.US_ASCII);
            output.write(data);
            output.flush();

            log.info("REQUEST STRING :: {}", reqString);
            log.info("SOCKET PORT(target) :: {}", socket.getPort());
            log.info("SOCKET LOCAL PORT(me) :: {}", socket.getLocalPort());
            log.info("SENT DATA :: edge IP - {}, dataid - {}", edge.getIP(), dataid);

            input = socket.getInputStream();

            byte[] bytes = new byte[200];
            int readByteCount = input.read(bytes);
            String receivedMsg = new String(bytes, 0, readByteCount, "UTF-8");
            log.info("RECEIVED MESSAGE :: msg - {}", receivedMsg);

            if(receivedMsg.toLowerCase().contains("fail")) {
                log.warn("SPREAD FAIL :: dataid - {}", dataid);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /*
     * (Edge -> Edge) ???????????? & MetaData ?????? ??? HashTable ?????? ????????????
     * */
    @Transactional
    @SneakyThrows
    public void saveMetaHashFromEdge(LocalDateTime receivingTime, String uuid, MultipartFile datafile, MultipartFile vehicleCertFile, MetaData metaData) {

        Hash hash = Hash.builder()
                .sourceId(Sender.NODE.getValue() + uuid)                     // ????????? ?????? ?????????
                .dataId(metaData.getDataId())                                // ????????? ????????? ?????????
                .destinationId(Sender.NODE.getValue() + edgeInfo.getUuid())  // ????????? ?????? ?????????
                .timestamp(receivingTime)                                    // ?????? ??????
                .build();

        hashService.save(hash);
        metaDataService.save(metaData);

        // (4) ????????? ?????? ??????
        fileManager.saveFileFromEdge(datafile, metaData.getDataId());

        // (5) ????????? ??????
        // ???????????? ????????? ?????? ??????(or ??????)
        fileManager.makeAllDiretories(fileManager.getSavingVehicleCertPath());
        // TODO :: ?????? ????????? ????????????????????? ??????????????? ??????(???????????? ?????? ?????? ?????? ??????. metadata??? cert, directory??? ????????? ??? ??????)
        Files.copy(vehicleCertFile.getInputStream(), Paths.get(metaData.getCert()), StandardCopyOption.REPLACE_EXISTING);

        // MEMO :: edge to edge??? ????????? ????????? history tracking??? ?????? hashtabl??? central??? ??????
        sendHashToCentral(hash);
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

        log.info("Spread ?????? Edge : {}, {}", edge.name(), edge.getIP());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = convertObjectToMultiValueMap(metaData, hash);
        body.add("uuid", edgeInfo.getUuid());
        body.add("datafile", new FileSystemResource(dataFilePath));
        body.add("certfile", new FileSystemResource(certFilePath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        ResponseEntity response = restTemplate.postForEntity("https://" + edge.getIP() + ":8443/api/edge/upload/edge/", requestEntity, String.class);

        log.info("Edge {} - {} :: ?????? ?????? :: {} ", edge.name(), edge.getIP(), response);

    }


    @SneakyThrows
    public void sendFilesToCentral(String dataFilePath, String certFilePath, MetaData metaData, Hash hash) {

        log.info("------ sendFilesToCentral start ------");

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // file[] : [0]-datafile, [1]-certfile, [2]-metadata, [3]-hash ?????? ??????

        ObjectMapper objectMapper = new ObjectMapper();

        File datafile = new File(dataFilePath);
        File certfile = new File(certFilePath);

        // byte[] meta = objectMapper.writeValueAsString(metaData).getBytes(StandardCharsets.UTF_8);
        // byte[] hashtable = objectMapper.writeValueAsString(hash).getBytes(StandardCharsets.UTF_8);

        String metaJson = objectMapper.writeValueAsString(metaData);
        String hashJson = objectMapper.writeValueAsString(hash);

        body.add("file", new FileSystemResource(datafile));
        body.add("file", new FileSystemResource(certfile));

        body.add("metadata", metaJson);
        body.add("hashtable", hashJson);

        body.add("signature", getSignatureResource(datafile));
        body.add("signature", getSignatureResource(certfile));
        body.add("signature", getSignatureResource(metaJson));
        body.add("signature", getSignatureResource(hashJson));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        // TODO :: central ip,port ????????? ?????? ??????
        String centralUrl = "https://20.196.220.98:8443/api/central/add/data/auth";
        // String centralUrl = "https://127.0.0.1:8089/api/central/add/data/auth";

        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        log.info("sendFilesToCentral(Auth) response :: {}", response);
        log.info("------ sendFilesToCentral end ------");


    }


    /*
    * ???????????? central web?????? datafile ??????????????? history??? ?????? datafile??? ?????? edge?????? tracing history??? ????????????
    * ??? ????????? central??? ??????
    * */
    public ResponseEntity<String> saveAndsendDownloadHashToCentral(Hash hash) {
        hashService.save(hash);
        return sendHashToCentral(hash);
    }

    @SneakyThrows
    public ResponseEntity<String> sendHashToCentral(Hash hash) {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        String hashJson = objectMapper.writeValueAsString(hash);

        body.add("hashtable", hashJson);
        body.add("signature", getSignatureResource(hashJson));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);

        // TODO :: central ip,port ????????? ?????? ??????
        String centralUrl = "https://20.196.220.98:8443/api/central/add/item/auth";
        // String centralUrl = "https://127.0.0.1:8089/api/central/add/item/auth";

        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        log.info("sendHashToCentral(Auth) response :: {} ", response);

        return response;
    }


    /*
    * EdgeSupporter??? ?????? ???????????? ??????????????? ?????? db????????? ??????
    * */
    @Transactional
    public void deleteMetaAndHash(String dataid) {
        fileManager.removeFile(dataid, "csv.gz");
        hashService.delete(dataid);
        metaDataService.delete(dataid);
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

    @SneakyThrows
    private PrivateKey getPrivateKey() {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(certificateProperties.getKeyStore()), certificateProperties.getKeyStorePassword().toCharArray());
        // keyStore.load(new FileInputStream("/Users/penta/IdeaProjects/cloudEdge/transmitter/src/main/resources/client-key.jks"), this.keyPassword.toCharArray());
        return (PrivateKey) keyStore.getKey(certificateProperties.getKeyAlias(), certificateProperties.getKeyStorePassword().toCharArray());
    }


    @SneakyThrows
    private ByteArrayResource getSignatureResource(byte[] bytes) {
        // ??????
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivateKey());

        signature.update(bytes);

        byte[] digitalSignature = signature.sign();

        // * Getter ??????
        ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
            @Override
            public String getFilename() {
                return "signature";
            }
        };

        return resource;

    }

    @SneakyThrows
    private ByteArrayResource getSignatureResource(String string) {

        // ??????
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivateKey());

        signature.update(string.getBytes(StandardCharsets.UTF_8));
        byte[] digitalSignature = signature.sign();

        // * Getter ??????
        ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
            @Override
            public String getFilename() {
                return "signature";
            }
        };

        return resource;

    }

    @SneakyThrows
    private ByteArrayResource getSignatureResource(File file) {
        // ??????
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivateKey());

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        signature.update(fileBytes);

        byte[] digitalSignature = signature.sign();

        // * Getter ??????
        ByteArrayResource resource = new ByteArrayResource(digitalSignature) {
            @Override
            public String getFilename() {
                return "signature";
            }
        };

        return resource;
    }


}
