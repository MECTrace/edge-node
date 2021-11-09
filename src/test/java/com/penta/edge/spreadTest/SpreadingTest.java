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

        String datafilePath = "/Users/penta/edgecloud_file/vehicle/5.2_Chevolet-Bolt-BMS_02구2392_1G1F76E0XJ4114544_2021-08-06T12_00_00.000.csv.gz";
        String certfilePath = "/Users/penta/edgecloud_file/cert/Vehicle/556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0.crt";

        MetaData metaData = MetaData.builder()
                .dataId("02e6a8e21528dabb17f3875eb47f3e8cda15dca30b76c39dbc6a2e19e631a0aa")
                .availabilityPolicy(Integer.valueOf(1))
                .cert("/Users/penta/edgecloud_file/cert/Vehicle/556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0.crt")
                .dataPriority(Integer.valueOf(1))
                .dataSignature("533cf1e6653f2a8e53e4cec34cff91cbedbff4c70c16f61120bea8e50bd9da7277a5f27c9c0e206685ad959073282d26ac2b5a1f70ff1afc0556e71b1c391fbc4c51d0e102965fd97cc1deb9157a309a01431184bfd0b606b2d61ddf42496729f5823f9f0c8b3a720f62bc7879f57015fd3b6538b1aa32ac50fdc9e63c4e312755d87f096c0a5541a274ffd1e1260546f5a43661d1677310ca27e820fff7bf95db4aacb0f53b5bb4795027dd0c8d7597d9304960ea13f100dd7eaf5a1946927fee767700c7d195f8a4675c1ad44e57f363b39b1e98607955678d118957e966955fd8d8d2472a83c8ae62fd891132f509aa206b3fdda27472543bf0c3a66560c8")
                .dataSize(7L)
                .dataType(0)
                .directory("/Users/penta/edgecloud_file/vehicle")
                .fileType("csv")
                .linkedEdge(null)
                .securityLevel(Integer.valueOf(4))
                .timestamp(LocalDateTime.now())
                .build();

        Hash hash = Hash.builder()
                        .dataId("02e6a8e21528dabb17f3875eb47f3e8cda15dca30b76c39dbc6a2e19e631a0aa")
                        .destinationId("0123456789900")
                        .sourceId("01556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0")
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
