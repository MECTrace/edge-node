package com.penta.edge.controller;

import com.penta.edge.constant.Sender;
import com.penta.edge.domain.MetaData;
import com.penta.edge.process.EdgeProcess;
import com.penta.edge.service.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge")
@Slf4j
public class DataController {

    private final FileManager fileManager;
    private final EdgeProcess edgeProcess;

    // TODO : 송신자별 URL생성 필요 (/upload/{sender})
    @PostMapping(value = "/upload/vehicle")
    public ResponseEntity<?> getFile(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("signature") MultipartFile[] signatures,
            HttpServletRequest request) throws Exception {

        // 데이터 수신시간 Timestamp
        LocalDateTime receivingTime = LocalDateTime.now();

        // Request의 인증서 추출
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        X509Certificate certificate = certs[0];

        for (int i = 0; i < files.length; i++) {
            // 수신 데이터 파일명 format Sample : 5_1_Hyundai_Ionic2017_34머0364_KMHC051HFHU000615_2021_05_03T13_21

            MultipartFile file = files[i];
            MultipartFile signatureBytes = signatures[i];   // file배열과 1:1대응되는 전자서명 추출

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(certificate.getPublicKey());
            signature.update(file.getBytes());

            // 전자서명 binary to hex
            String signatureValue = fileManager.getHex(signatureBytes.getBytes());

            // 전자서명 검증
            if (signature.verify(signatureBytes.getBytes())) {
                // 서명 검증 성공
                log.info("VERIFIED SIGNATURE");
                // 파일명에 포함된 데이터 추출을 위해 확장자가 있을 시 제거
                String fileName = file.getOriginalFilename().contains(".") ?
                        file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                        : file.getOriginalFilename();

                // 파일명에서 Timestamp 추출 : 2021_05_03T13_21 > LocalDateTime으로 변환
                String dateTimeStr = fileName.substring(fileName.length() - 16, fileName.length());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm");
                LocalDateTime timestamp = LocalDateTime.parse(dateTimeStr, formatter);

                String fileHash = fileManager.getHash(file);

                // Meta data 생성 및 DB저장(Meta Data, Hash Table)
                edgeProcess.saveMetaDataNHashTable(file, certificate, Sender.VEHICLE, receivingTime,
                        MetaData.builder()
                        .dataID(fileHash)                           // 데이터 파일의 해시 값
                        .timestamp(timestamp)                       // 데이터 파일 생성 시간
                        .fileType("csv")                            // 데이터 파일 타입
                        .dataType(Integer.valueOf(0))
                        .securityLevel(Integer.valueOf(1))
                        .dataPriority(Integer.valueOf(1))
                        .availabilityPolicy(Integer.valueOf(1))
                        .dataSignature(signatureValue)              // 데이터 파일 전자서명 값
                        .cert(null)                                 // 인증서 저장 위치(파일명 포함) >> endgeProcess에서 초기화
                        .directory(fileManager.getVehicleLocation().toString()) // 데이터 파일 저장 위치(파일명 제외)
                        .linkedEdge(null)                           // 데이터 파일 원본을 소유하고 있는 Edge Node의 UUID
                        .dataSize(file.getSize() / 1024)            // 데이터 파일 크기
                        .build()
                );
            } else {
                // 서명이 일치하지 않은 경우
                log.error("FAILED TO VERIFY");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } // end of for
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
