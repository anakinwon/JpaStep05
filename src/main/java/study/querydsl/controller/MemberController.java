package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    /**
     *  <Test URL>
     *      - 전체 조회 : http://localhost:8080/v1/members
     *      - 조건 조회 : 나이가 30살 보다 어리면서, 팀은 ATEAM 회원만 검색하기.
     *                  http://localhost:8080/v1/members?ageLoe=30&teamName=ATEAM
     *
     * */
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    /**
     *  <Simple Page Test URL>
     *      - 전체 조회 : http://localhost:8080/v2/members
     *      - 조건 조회 : 나이가 30살 보다 어리면서, 팀은 ATEAM 회원만 검색하기.
     *                  http://localhost:8080/v2/members?page=0&size=2
     *
     * */
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }



    /**
     *  <Complex Page Test URL>
     *      - 전체 조회 : http://localhost:8080/v3/members
     *      - 조건 조회 : 나이가 30살 보다 어리면서, 팀은 ATEAM 회원만 검색하기.
     *                  http://localhost:8080/v3/members?page=1&size=5
     *
     * */
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageCount(condition, pageable);
    }

    /**
     *  <Count Page Test URL>
     *      - 전체 조회 : http://localhost:8080/v4/members
     *      - 조건 조회 : 마지막 페이지를 자동 감지하면 전체건수 조회는 하지 않아서 성능에 유리함.
     *                  http://localhost:8080/v4/members?page=0&size=120
     *
     * */
    @GetMapping("/v4/members")
    public Page<MemberTeamDto> searchPageCount(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageCount(condition, pageable);
    }


}
