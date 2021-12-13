package com.penta.edge.controller;

import com.penta.edge.constant.EdgeInfo;
import com.penta.edge.constant.EdgeNode;
import com.penta.edge.domain.Hash;
import com.penta.edge.domain.MetaData;
import com.penta.edge.domain.RequestDto;
import com.penta.edge.process.EdgeProcess;
import com.penta.edge.service.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final EdgeInfo edgeInfo;


    @PostMapping(value = "/upload/vehicle")
    public ResponseEntity<?> getFile(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("signature") MultipartFile[] signatures,
            HttpServletRequest request) throws Exception {

        log.info("**************** DEVICE DIRECT **************** ");

        // 데이터 수신시간 Timestamp
        LocalDateTime receivingTime = LocalDateTime.now();

        /*
        if(request.getHeader("host").startsWith("localhost")) {
            return ResponseEntity.ok("ok");
        }
         */


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
                    dateTimeStr = matcher.group()+"_00.000";
                    formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss.SSS");
                } else {
                    return new ResponseEntity("파일명에서 유효한 TimeStamp를 찾을 수 없습니다.",HttpStatus.BAD_REQUEST);
                }

                LocalDateTime timestamp = LocalDateTime.parse(dateTimeStr, formatter);

                //String tmpTimeStr = LocalDateTime.parse(dateTimeStr, formatter).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                //LocalDateTime timestamp = LocalDateTime.parse(tmpTimeStr);

                String fileHash = fileManager.getHash(file);

                // V&V 전용 file 프로세스 진행 제외
                // bbf5d300ebc83979d54286aa920cc0a1b8c92d3dfff022e0a7332352fbad8128
                // 5.1_Hyundai-Kona-Kia-Niro-BMS_33라8981_KNACC81GFKA024613_2021-08-13T08_00_00.000.csv.gz
                if(fileHash.equals("bbf5d300ebc83979d54286aa920cc0a1b8c92d3dfff022e0a7332352fbad8128")) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }

                double fileSize = file.getSize()/1024;

                // Meta data 생성 및 DB저장(Meta Data, Hash Table)
                EdgeNode[] targetEdges = edgeProcess.saveMetaHashFromVehicle(file, certificate, receivingTime,
                        MetaData.builder()
                        .dataId(fileHash)                           // 데이터 파일의 해시 값
                        .timestamp(timestamp)                       // 데이터 파일 생성 시간
                        .fileType("csv.gz")                            // 데이터 파일 타입
                        .dataType(Integer.valueOf(0))
                        .securityLevel(Integer.valueOf(4))
                        .dataPriority(Integer.valueOf(1))
                        .availabilityPolicy(Integer.valueOf(1))
                        .dataSignature(signatureValue)              // 데이터 파일 전자서명 값
                        .cert(null)                                 // 인증서 저장 위치(파일명 포함) >> endgeProcess에서 초기화
                        .directory(fileManager.getVehicleLocation().toString()) // 데이터 파일 저장 위치(파일명 제외)
                        .linkedEdge(null)                           // 데이터 파일 원본을 소유하고 있는 Edge Node의 UUID
                        .dataSize((long)Math.ceil(fileSize))        // 데이터 파일 크기(올림)
                        .build()
                );


                // 파일, DB저장 후 KETI Socket 통신
                edgeProcess.sendToEdge(fileHash, targetEdges[0]);
                edgeProcess.sendToEdge(fileHash, targetEdges[1]);


            } else {
                // 서명이 일치하지 않은 경우
                log.error("FAILED TO VERIFY");
                return new ResponseEntity<>("전자서명이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
            }
        } // end of for
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /*
    * supporter에서 호출하는 API : 다운로드 받은 client의 IP와 다운로드 시간을 받아 tracing history 생성

    * central web에서 datafile download클릭 시 supporter API가 호출되고
    * supporter API는 본 API를 호출. 이후 tracing history를 생성하여 central로 해당 데이터 전송
    *
    * */
    @PostMapping(value = "/make/history")
    public ResponseEntity<?> getDataFromEdgeSupporter(
            @RequestParam String dataid,
            @RequestParam String clientip,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadtime) {

        ResponseEntity<String> response = edgeProcess.sendHashToCentral(Hash.builder()
                .sourceId(clientip)
                .destinationId(edgeInfo.getName())
                .dataId(dataid)
                .timestamp(downloadtime)
                .build());

        if(response.getStatusCodeValue()!=200) {
            log.warn("edge에는 tracing history가 정상적으로 저장되었으나 central로 전송하는데 실패함");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /*
    * edge1(penta) -> edge2(penta) https 컨트롤러.
    * edge1(penta) -> edge1(keti) -> edge2(keti) -> edge2(penta) socket 통신으로 수정하면서 사용되지 않음.
    * "/api/edge/upload/edge
    * */
    @PostMapping(value = "/upload/edge")
    public ResponseEntity<?> getFile(RequestDto req, HttpServletRequest request) {

        log.info("**************** FROM EDGE **************** ");

        // TODO : edge to edge 데이터 보낼 때 전체 object에 대한 전자서명 적용 후 인증서로 검증하는 과정 추가 필요(HttpServletRequest에서 추출)

        // 데이터 수신시간 Timestamp
        LocalDateTime receivingTime = LocalDateTime.now();

        // metadata > 그대로 저장
        MetaData metaData = req.getMetadata().toMetaData();
        // LIST로 받지만 인덱스는 1개임.
        edgeProcess.saveMetaHashFromEdge(receivingTime, req.getUuid(), req.getDatafile().get(0), req.getCertfile().get(0), metaData);


        return new ResponseEntity<>(HttpStatus.OK);

    }


}
