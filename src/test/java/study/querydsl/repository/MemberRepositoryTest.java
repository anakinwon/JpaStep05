package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        // Given
        Team teamA = new Team("ATEAM");
        Team teamB = new Team("BTEAM");
        Team teamC = new Team("CTEAM");

        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);

        Member member1 = new Member("Yoda"  , 224, teamB);
        Member member2 = new Member("Qwigon",125, teamC);
        Member member3 = new Member("Obiwan", 83 , teamB);
        Member member4 = new Member("Anakin", 28 , teamA);
        Member member5 = new Member("AsoKa" ,22, teamC);
        Member member6 = new Member("Padme" , 32 , teamA);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);

        // 회원이름과 팀이름이 같은 사람 생성.
        em.persist(new Member("ATEAM", 120, teamA));
        em.persist(new Member("BTEAM", 130, teamB));
        em.persist(new Member("CTEAM", 140, teamC));

        // 영속성컨텍스트에 있는 쿼리를 즉시 수행한다.
        em.flush();
        // 저장 후 영속성컨텍스트에 있는 캐시 삭제
        em.clear();

    }



    /**
     *   <실무 활용 - 스프링 데이터 JPA와 Querydsl>
     *       - 스프링 데이터 JPA 리포지토리로 변경
     *          : 스프링 데이터 JPA - MemberRepository 생성
     *
     * */

    @Test
    void findByUsername() {

        Team dteam = new Team("DTEAM");
        em.persist(dteam);

        // 1. 스프링 데이터 JPA - 저장 테스트
        Member member  = new Member("Duke", 128, dteam);
        Member member2 = new Member("Grievous", 92, dteam);
        memberRepository.save(member);
        memberRepository.save(member2);

        // 2. 스프링 데이터 JPA - PK ID조회 테스트
        Member findMember = memberRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);
        System.out.println("\t2. findMember = " + findMember);

        // 3.1 스프링 데이터 JPA - 전체 조회 테스트
        List<Member> findAll = memberRepository.findAll();
        System.out.println("\t\t3.1 findAll = " + findAll);

        // 4.1 스프링 데이터 JPA - 회원명으로 조회 테스트 (findByUsername  <= 사용자 정의함수)
        List<Member> findByUsername = memberRepository.findByUsername("Duke");
        System.out.println("\t\t\t4.1 findBuUsername = " + findByUsername);


        // 5.1 스프링 데이터 JPA - 팀명으로 회원 조회 테스트 (findByTeamName  <= 사용자 정의함수)
        List<Member> resultTeam = memberRepository.findByTeamName("CTEAM");
        for (Member teamMemeber : resultTeam) {
            System.out.println(" 5.1 resultTeam  = " + teamMemeber);
        }

    }



    /**
     *   <실무 활용 - 사용자 정의 리포지토리>
     *       - 사용자 정의 리포지토리 사용법
     *          1. 사용자 정의 인터페이스 작성   MemberRepositoryCustom.java
     *          2. 사용자 정의 인터페이스 구현   MemberRepositoryImpl.java
     *          3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
     *
     * */

    @Test
    void searchTest() {

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setUsername(null);
        condition.setTeamName("CTEAM");
        condition.setAgeGoe(100);
//        condition.setAgeLoe(300);

        // 4.1 스프링 데이터 JPA - 회원명으로 조회 테스트 (search  <= 사용자 정의함수)
        List<MemberTeamDto> findByUsername = memberRepository.search(condition);
        for (MemberTeamDto memberTeamDto : findByUsername) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }
    }


    /**
     *   <심플 페이징처리>
     *       - 간단한 페이지 예제
     *
     * */
    @Test
    void pageSimple() {
        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setUsername(null);
//        condition.setTeamName("CTEAM");
//        condition.setAgeGoe(100);
//        condition.setAgeLoe(300);

        PageRequest pageRequest = PageRequest.of(0,3);

        // 6. 간단한 페이지 조회하기.
        Page<MemberTeamDto> findByPage = memberRepository.searchPageSimple(condition, pageRequest);

        for (MemberTeamDto memberTeamDto : findByPage) {
            System.out.println(" \t\t\t SimplePage MemberList = " + memberTeamDto);
        }
    }


    /**
     *   <복잡한 페이징처리>
     *       - 복잡한 페이지 예제
     *       - 내용과 전체 건 수를 분리해는 기술
     *       - 성능최적화를 위해서 카운트는 필요없을 경우 유용함.
     *
     * */

    @Test
    void pageComplex() {

        MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(1,3);

        // 6. 간단한 페이지 조회하기.
        Page<MemberTeamDto> findByPage = memberRepository.searchPageComplex(condition, pageRequest);

        for (MemberTeamDto memberTeamDto : findByPage) {
            System.out.println(" \t\t\t ComplexPage MemberList = " + memberTeamDto);
        }
    }


    /**
     *   <페이징 최적화 처리>
     *       - 마지막 페이지가 자동 인식되면,
     *       - 카운트 쿼리를 수행되지 않으므로 최적화 됨.
     *
     * */

    @Test
    void pageCount() {

        MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(1,5);

        // 6. 간단한 페이지 조회하기.
        Page<MemberTeamDto> findByPage = memberRepository.searchPageCount(condition, pageRequest);

        for (MemberTeamDto memberTeamDto : findByPage) {
            System.out.println(" \t\t\t Page Without Count = " + memberTeamDto);
        }
    }

    /**
     * <스프링 데이터 JPA가 제공하는 Querydsl 기능>
     *     - 여기서 소개하는 기능은 제약이 커서 복잡한 실무 환경에서 사용하기에는 많이 부족하다.
     *     - 그래도 스프링 데이터에서 제공하는 기능이므로 간단히 소개하고, 왜 부족한지 설명하겠다.
     *
     *
     *     <한계점>
     *       - 조인X (묵시적 조인은 가능하지만 left join이 불가능하다.)
     *       - 클라이언트가 Querydsl에 의존해야 한다.
     *       - 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 한다.
     *       - 복잡한 실무환경에서 사용하기에는 한계가 명확하다.
     *
     *       - 결국... 쓸데없다.
     *
     * */
    @Test
    public void querydslPredicateExcutorTest() {
        QMember member = QMember.member;

        Iterable<Member> result = memberRepository.findAll(member.age.between(20, 30).and(member.username.eq("Anakin")));

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

    }

}