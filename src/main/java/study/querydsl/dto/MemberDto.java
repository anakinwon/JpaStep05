package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본생성자를 반드시 써야 하므로,
public class MemberDto {
    private String username;
    private int age;

    // 기본생성자를 쓰거나
//    public MemberDto() {
//    }

    // 생성자
    @QueryProjection      // @QueryProjection DTO 까지 Q파일로 생성되도록 하는 옵션
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}