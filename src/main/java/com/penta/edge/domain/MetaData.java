package com.penta.edge.domain;


import com.penta.edge.constant.AvailabilityPolicy;
import com.penta.edge.constant.DataPriority;
import com.penta.edge.constant.SecurityLevel;
import lombok.*;
import org.hibernate.annotations.Check;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.sql.Time;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_management")
public class MetaData {

    // 데이터 파일의 해시값
    @Id
    private String dataID;

    // 데이터 파일의 생성시간(파일명에 포함된 시간을 파싱)
    @Column(nullable = false)
    @Convert(converter= TimeToStringConverter.class)
    private LocalDateTime timestamp;

    // 데이터 파일 타입(csv)
    @Column(nullable = false)
    private String fileType;

    // 0:데이터 원본 보유, 1:링크를 통한 간접 접근
    @Column(nullable = false)
    @Min(0)
    @Max(1)
    private Integer dataType;

    /*
    * 1:링크를 통한 간접 접근만 허용 /읽기만 허용
    * 2:링크를 통한 간접 접근만 허용 /읽기&쓰기 허용
    * 3:링크를 통한 간접 접근만 허용 /읽기&쓰기&삭제 허용
    * 4:원본 공유 허용 / 1:1공유(추적가능)
    * 5:원본 공유 허용 / N:1분산공유(추적미지원)
    * *1~4등급은 데이터 추적 지원 / 5등급은 미지원
    *
    * */
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer securityLevel;

    // 데이터 중요도
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer dataPriority;

    // 데이터 가용성 확보 정책
    @Column(nullable = false)
    @Min(1)
    @Max(2)
    private Integer availabilityPolicy;

    // 데이터 파일 전자서명 값
    @Column(length=2000)
    private String dataSignature;

    /*
    * 인증서 저장 경로(~ /[Type]/[ID])
     * TYPE : Vehicle, Node, Central, User
     * ID : Vehicle or User - 인증서의 Public key / Node or Central - uuid
    * */
    @Column(nullable = false, length = 4000)
    private String cert;

    // 데이터 파일 저장 위치(dataType = 1 인경우 NULL)
    private String directory;

    // 데이터 파일 원본을 소유하고 있는 Edge Noded의 UUID
    private String linkedEdge;

    // 데이터 파일 크기
    @Column(nullable = false)
    private Long dataSize;

}
