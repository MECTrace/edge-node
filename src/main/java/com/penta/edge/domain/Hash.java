package com.penta.edge.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Table(name = "tracing_history")
@Setter
@Getter
public class Hash {

    // 데이터 파일의 해시값
    @Id
    @Column(name = "dataid")
    private String dataId;

    // 데이터 파일 수신 시간(데이터를 수신한 서버시간)
    @Column(name = "timestamp",nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp;

    /*
    * 데이터 파일 송신자(TYPE+ID)
    * TYPE : Vehicle(0x01), Edge(0x02), Central(0x03), User(0x04)
    * ID : Vechile or User - 인증서의 Public Key
    *      Edge & Central - UUID
    * ex : 0x010000daaaadddd....
    * */
    @Column(name="sourceid", nullable = false, length = 2000)
    private String sourceId;

    /*
     * 데이터 파일 수신자(TYPE+ID)
     * TYPE : Vehicle(0x01), Edge(0x02), Central(0x03), User(0x04)
     * ID : Vechile or User - 인증서의 Public Key
     *      Edge & Central - UUID
     * * ex : 0x020000daaaadddd....
     * */
    @Column(name = "destinationid", nullable = false, length = 2000)
    private String destinationId;


    @Override
    public String toString() {
        return "Hash{" +
                "dataId='" + dataId + '\'' +
                ", timestamp=" + timestamp +
                ", sourceId='" + sourceId + '\'' +
                ", destinationId='" + destinationId + '\'' +
                '}';
    }
}
