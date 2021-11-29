package com.penta.edge.spreadTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penta.edge.domain.Hash;
import com.penta.edge.domain.MetaData;
import com.penta.edge.process.EdgeProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootTest
public class SpreadingTest {


    @Autowired
    EdgeProcess edgeProcess;

    @Test
    public void sendToControllerTest() {

        String datafilePath = "/Users/penta/edgecloud_file/vehicle/5.2_Hyundai-Ionic2020_21부2270_KMHC851JFLU067889_2021-10-22T08_17_00.000.csv.gz";
        String certfilePath = "/Users/penta/edgecloud_file/cert/Vehicle/44eb2f8d2f9aa98cd394570d1be096a68282f2b0c9027c250254614c2bbb9709.crt";

        MetaData metaData = MetaData.builder()
                .dataId("71a338dc6281ff3647077adb36f7b14b12f75d3e010b1a4a44491f37748a3873")
                .availabilityPolicy(Integer.valueOf(1))
                .cert("/Users/penta/edgecloud_file/cert/Vehicle/44eb2f8d2f9aa98cd394570d1be096a68282f2b0c9027c250254614c2bbb9709.crt")
                .dataPriority(Integer.valueOf(1))
                .dataSignature("370b7622b70411f4c53dc802014280832863be8f44970c1e3c0ee9a2e7f0c711fa500ff10d3267ef450db2b68f3309c9ebbc60f5238f2b612b7ba1ede65574ac309849ea4c4c6e7ab61d364cf62eaeeb63cd31a0c9d931ad9e7f0e5e05d12293a32f3e147caf1d90391bb5edb5a004449001777e9d531402953de6604c80e0471fbe153f832ef3e65987ac2c89bf9db3995d1acc7538ed867f896eef90ec6e22bf19c6fccf90d9cba63b518944d6bc0a65855a5481ef070efaf4f1ad26fa1dccb645ace760e723b929999c59eb03878743781bdea93f676123f2362c97fcdc02b7f7e062ad313326f2b8ad55e4cd2beb2c95b44cb9f76735e0792f29d6ab885b")
                .dataSize(7L)
                .dataType(0)
                .directory("/Users/penta/edgecloud_file/vehicle")
                .fileType("csv")
                .linkedEdge(null)
                .securityLevel(Integer.valueOf(4))
                .timestamp(LocalDateTime.now())
                .build();

        Hash hash = Hash.builder()
                        .dataId("71a338dc6281ff3647077adb36f7b14b12f75d3e010b1a4a44491f37748a3873")
                        .destinationId("0123456789900")
                        .sourceId("0144eb2f8d2f9aa98cd394570d1be096a68282f2b0c9027c250254614c2bbb9709")
                        .timestamp(LocalDateTime.now())
                        .build();


        // edgeProcess.sendFilesToCentral(datafilePath, certfilePath, metaData, hash);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body= new LinkedMultiValueMap<>();

        body.add("datafile", new FileSystemResource(datafilePath));
        body.add("certfile", new FileSystemResource(certfilePath));

        ObjectMapper objectMapper = new ObjectMapper();

        try{
            body.add("metadata", objectMapper.writeValueAsString(metaData));
            body.add("hashtable", objectMapper.writeValueAsString(hash));
        } catch(Exception e) {
            e.printStackTrace();
        }

        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body, header);

        String centralUrl = "http://20.196.220.98:80/api/tracking/add/data";
        System.out.println(requestEntity.getBody().toString());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        System.out.println(response);

    }


    @Test
    public void sendAddItem() {
        Hash hash = Hash.builder()
                .dataId("02e6a8e21528dabb17f3875eb47f3e8cda15dca30b76c39dbc6a2e19e631a0aa")
                .destinationId("022223456789900")
                .sourceId("01556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0")
                .timestamp(LocalDateTime.now())
                .build();


        // edgeProcess.sendHashToCentral(hash);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String centralUrl = "http://20.196.220.98:80/api/tracking/add/item";

        MultiValueMap<String, Object> body= new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            body.add("hashtable", objectMapper.writeValueAsString(hash));
        } catch(Exception e) {
            e.printStackTrace();
        }

        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body, header);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(centralUrl, requestEntity, String.class);

        System.out.println(response);

    }




}
