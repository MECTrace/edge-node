package com.penta.edge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Name;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Table(name = "tracing_history")
public class Hash {

    // 데이터 파일의 해시값
    @Id
    @Column(name = "dataID")
    private String dataID;

    // 데이터 파일 수신 시간(데이터를 수신한 서버시간)
    @Column(name = "timestamp",nullable = false)
    private LocalDateTime timestamp;

    /*
    * 데이터 파일 송신자(TYPE+ID)
    * TYPE : Vehicle(0x01), Edge(0x02), Central(0x03), User(0x04)
    * ID : Vechile or User - 인증서의 Public Key
    *      Edge & Central - UUID
    * ex : 0x010000daaaadddd....
    * */
    @Column(name="sourceID", nullable = false, length = 2000)
    private String sourceID;

    /*
     * 데이터 파일 수신자(TYPE+ID)
     * TYPE : Vehicle(0x01), Edge(0x02), Central(0x03), User(0x04)
     * ID : Vechile or User - 인증서의 Public Key
     *      Edge & Central - UUID
     * * ex : 0x020000daaaadddd....
     * */
    @Column(name = "destinationID", nullable = false, length = 2000)
    private String destinationID;


}
