package study.querydsl.dto;

import lombok.Data;

// 검색 조건 : 어드민화면에서 조건을 가지고 검색하는 기능
@Data
public class MemberSearchCondition {
    // 회원명, 팀명, 나이(ageGoe, ageLoe)
    // MemberJpaRepository에 생성한다.

    private String username;   // 회원명 검색 조건
    private String teamName;   // 팀명 검색 조건
    private int ageGoe;        // ~보다 큰나이 검색 조건
    private int ageLoe;        // ~보다 작은나이 검색 조건
}
