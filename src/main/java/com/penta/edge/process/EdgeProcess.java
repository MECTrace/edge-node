package com.penta.edge.process;

import com.penta.edge.constant.EdgeInfo;
import com.penta.edge.constant.Sender;
import com.penta.edge.domain.Hash;
import com.penta.edge.domain.MetaData;
import com.penta.edge.service.FileManager;
import com.penta.edge.service.HashService;
import com.penta.edge.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EdgeProcess {

    private final FileManager fileManager;
    private final MetaDataService metaDataService;
    private final HashService hashService;
    private final EdgeInfo edgeInfo;

    /*
    * 파일저장 & MetaData 저장 및 HashTable 생성 프로세스
    * */
    @Transactional
    @SneakyThrows
    public void saveMetaDataNHashTable(
            MultipartFile file, X509Certificate certificate, Sender sender,
            LocalDateTime receivingTime, MetaData metaData) {

        // Public Key to Hex to hash(sha256)
        String pubKeyEncoded = fileManager.getHash(fileManager.getHex(certificate.getPublicKey().getEncoded()));

        // 인증서가 저장될 경로(파일명 포함)
        Path certPath = fileManager.getSavingVehicleCertPath(pubKeyEncoded+".crt");
        // 인증서 저장 위치 초기화
        metaData.setCert(certPath.toString());
        metaDataService.save(metaData);
        // (3) HashTable 저장
        hashService.save(Hash.builder()
                .sourceID(sender.getValue()+pubKeyEncoded)        // 데이터 파일 송신자
                .dataID(metaData.getDataID())                     // 데이터 파일의 해시값
                .destinationID(edgeInfo.getName())                // 데이터 파일 수신자
                .timestamp(receivingTime)                         // 수신 시간
                .build()
        );
        // (4) 데이터 파일 저장
       fileManager.saveFileFromVehicle(file, metaData.getDataID());
        // (5) 인증서 저장
        if(sender.equals(Sender.VEHICLE)) {
            // 인증서가 저장될 폴더 확인(or 생성)
            fileManager.makeAllDiretories(fileManager.getSavingVehicleCertPath());
            Files.write(certPath, getCertString(certificate).getBytes());  // 인증서 저장
        } else if(sender.equals(Sender.NODE)) {
            // ... todo : 다른 송신자 분기 필요
        }
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


}
