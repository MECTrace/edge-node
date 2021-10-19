package com.penta.edge.controller;

import com.penta.edge.constant.Sender;
import com.penta.edge.domain.MetaData;
import com.penta.edge.process.EdgeProcess;
import com.penta.edge.service.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            /*
            * 아래 형식의 파일명에서만 동작
            *  format Sample(1) : 5_1_Hyundai_Ionic2017_34머0364_KMHC051HFHU000615_2021_05_03T13_21
            *  format Sample(2) : 5.2_Chevolet-Bolt-BMS_02구2392_1G1F76E0XJ4114544_2021-08-01T11_00_00.000
            * */

            MultipartFile file = files[i];
            MultipartFile signatureBytes = signatures[i];   // file 배열과 1:1대응되는 전자서명 추출

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(certificate.getPublicKey());
            signature.update(file.getBytes());

            // 전자서명 binary to hex
            String signatureValue = fileManager.getHex(signatureBytes.getBytes());

            // 전자서명 검증
            if (signature.verify(signatureBytes.getBytes())) {
                // 서명 검증 성공
                log.info("VERIFIED SIGNATURE");
                String fileName = file.getOriginalFilename();
                // 파일명에서 Timestamp 추출
                // Sample(1) 대응 : 5.2_Chevolet-Bolt-BMS_02구2392_1G1F76E0XJ4114544_2021-08-01T11_00_00.000
                Pattern patternWithMs = Pattern.compile("\\d{4}-[01]\\d-[0-9]*T.*\\d[0-2]\\d((_[0-5]\\d)?){2}");
                // Sample(2) 대응 : 5_1_Hyundai_Ionic2017_34머0364_KMHC051HFHU000615_2021_05_03T13_21
                Pattern patternWithoutMs = Pattern.compile("(\\d{4}_\\d{2}_\\d{2})[A-Z]+(\\d{2}_\\d{2})");

                Matcher matcherMs = patternWithMs.matcher(fileName);
                Matcher matcher = patternWithoutMs.matcher(fileName);

                String dateTimeStr = "";
                DateTimeFormatter formatter = null;

                if(matcherMs.find()) {
                    // 2021-08-01T11_00_00.000
                    dateTimeStr = matcherMs.group();
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");
                } else if(matcher.find()) {
                    // 2021_05_03T13_21
                    dateTimeStr = matcher.group()+".000";
                    formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm.SSS");
                } else {
                    return new ResponseEntity("파일명에서 유효한 TimeStamp를 찾을 수 없습니다.",HttpStatus.BAD_REQUEST);
                }

                LocalDateTime timestamp = LocalDateTime.parse(dateTimeStr, formatter);

                //String tmpTimeStr = LocalDateTime.parse(dateTimeStr, formatter).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                //LocalDateTime timestamp = LocalDateTime.parse(tmpTimeStr);

                String fileHash = fileManager.getHash(file);

                // Meta data 생성 및 DB저장(Meta Data, Hash Table)
                edgeProcess.saveMetaDataNHashTable(file, certificate, Sender.VEHICLE, receivingTime,
                        MetaData.builder()
                        .dataID(fileHash)                           // 데이터 파일의 해시 값
                        .timestamp(timestamp)                       // 데이터 파일 생성 시간
                        .fileType("csv")                            // 데이터 파일 타입
                        .dataType(Integer.valueOf(0))
                        .securityLevel(Integer.valueOf(4))
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
                return new ResponseEntity<>("전자서명이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
            }
        } // end of for
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
