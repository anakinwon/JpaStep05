package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


@Repository // DAO와 유사한 개념
public class MemberJpaRepository {

    // 순수 JPA는 EntityManager가 필요함.
    private final EntityManager em;

    // Query DSL을 사용하기 위해서 JPAQueryFactory가 필요함.
    private final JPAQueryFactory queryFactory;

    // 생성자가 필요함.
//    public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
//        this.em = em;
//        this.queryFactory = queryFactory;
//    }
    /**
     *  EntityManager, JPAQueryFactory 초기화 생성자.
     *
     * */
    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 1. 저장하기.
     *      : em.save()
     * */
    public void save(Member member) {
        em.persist(member);
    }

    /**
     * 2. PK값으로 조회하기.
     *      : em.find()
     * */
    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    /**
     * 3. 전체값으로 조회하기.
     *      : em.createQuery()    // 쿼리 String
     *          .getResultList()  // List 결과 반환
     * */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m ", Member.class)
                .getResultList();
    }
    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    /**
     * 4. 회원이름으로 조회하기.
     *      : em.createQuery()   // 쿼리 String
     *          .setParameter()  // 파라미터 세팅
     *          .getResultList() // List 결과 반환
     * */
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username",username)
                .getResultList();
    }
    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }



    /**
     *   <동적 쿼리와 성능 최적화 조회>
     *       1. Builder 사용 테스트
     *          - MemberTeamDto.java : 조회 최적화용 DTO 추가
     *          - MemberSearchCondition.java : 조회조건 생성
     *
     * */

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        // 조건 Builder 제작
        BooleanBuilder builder = new BooleanBuilder();

        // Ctrl + Shift + Enter (문장 자동완성)
        // Alt + Enter 후 Static import 로 줄여 준다.
        //if (StringUtils.hasText(condition.getUsername())) {
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() > 0) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() > 0) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                          member.id                      //  alias를 만들어 주지 않아도 된다.
                        //member.id.as("memberId")       //  alias를 줘도 된다.
                        , member.username
                        , member.age
                        , team.id                        //  alias를 만들어 주지 않아도 된다.
                        //, team.id.as("teamId")         //  alias를 줘도 된다.
                        , team.name                      //  alias를 만들어 주지 않아도 된다.
                        //, team.name.as("teamName")     //  alias를 줘도 된다.
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)                       //  조건을 빌더로 만들어서 적용한다.
                .fetch();
    }

    /**
     *   <동적 쿼리와 성능 최적화 조회>
     *       2. Where절 파라미터 사용
     *          - MemberTeamDto.java : 조회 최적화용 DTO 추가
     *          - MemberSearchCondition.java : 조회조건 생성
     *
     *       *** 강사가 강력히 추천하는 방식이다. ***
     *          - 조회 조건을 재사용가능하다는 장점
     *          - null 값 체크 후 조회 조건 조합이 가능하다.
     *
     * */
    public List<MemberTeamDto> search (MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                          member.id
                        , member.username
                        , member.age
                        , team.id
                        , team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(   null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                )
                .fetch();
    }

    // 참고 : where 절에 파라미터 방식을 사용하면 조건 재사용 가능
    // 재사용을 위해서 Predicate -> BooleanExpression 변경해서 사용할 것.
    // private Predicate usernameEq(String username) {
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }
    private BooleanExpression ageGoe(int ageGoe) {
        return ageGoe > 0 ? member.age.goe(ageGoe) : null ;
    }
    private BooleanExpression ageLoe(int ageLoe) {
        return ageLoe > 0 ? member.age.loe(ageLoe) : null ;
    }

}
