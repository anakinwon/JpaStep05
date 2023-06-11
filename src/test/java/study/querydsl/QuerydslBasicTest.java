package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class QuerydslBasicTest {

    @Autowired EntityManager em;
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

        Member member1 = new Member("Anakin", 28 , teamA);
        Member member2 = new Member("Padme" , 32 , teamA);
        Member member3 = new Member("Obiwan", 83 , teamB);
        Member member4 = new Member("Yoda"  , 224, teamB);
        Member member5 = new Member("Qwigon",125, teamC);
        Member member6 = new Member("AsoKa" ,22, teamC);
        em.persist(new Member("ATEAM", 120, teamA));
        em.persist(new Member("BTEAM", 130, teamB));


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);

        // 영속성컨텍스트에 있는 쿼리를 즉시 수행한다.
        em.flush();
        // 저장 후 영속성컨텍스트에 있는 캐시 삭제
        em.clear();

    }

    /**
     *  <JPQL 버전 테스트>
     * */
    @Test
    public void startJPQL() {
        // "Anakin" 찾는 SELECT 쿼리
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "Anakin")
                .getSingleResult();

        // 결과 검증
        assertThat(findMember.getUsername()).isEqualTo("Anakin");
    }

    /**
     *  <Querydsl버전 테스트>
     *      -  Tasks > other > compileQuerydsl ( 반드시 실행해야 함)
     *      - JPAQueryFactory를 사용한다.
     *
     * */
    @Test
    public void startQuerydsl() {
        QMember m = new QMember("m");

        // When : "Yoda" 를 조회한다.
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("Yoda"))
                .fetchOne();

        // Then : 조회 조건과 결과가 같은 지 검증한다.
        assertThat(findMember.getUsername()).isEqualTo("Yoda");
    }

    /**
     *  <QType 활용하기>
     *      -  Tasks > other > compileQuerydsl ( 반드시 실행해야 함)
     *      - JPAQueryFactory를 사용한다.
     *
     * */
    @Test
    public void startQuerydsl3() {
        //QMember m = new QMember("m");
        // 변경 가능
//        QMember m = QMember.member;
//        Member findMember = quertFactory
//                .select(m)
//                .from(m)
//                .where(m.username.eq("Yoda"))
//                .fetchOne();

//        // 변수 선언 없이 직접 "QMember.member" 사용 가능.
//        Member findMember = quertFactory
//                .select(QMember.member)
//                .from(QMember.member)
//                .where(QMember.member.username.eq("Yoda"))
//                .fetchOne();

        // static import 를 쓰면 선언을 안해도 된다.
        // Alt + Enter 누르면 QMember.member -> member 로 축약된다.
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("Yoda"))
                .fetchOne();

        // Then : 조회 조건과 결과가 같은 지 검증한다.
        assertThat(findMember.getUsername()).isEqualTo("Yoda");
    }

    /**
     *  <QType 활용하기>
     *      -  Tasks > other > compileQuerydsl ( 반드시 실행해야 함)
     *      - JPAQueryFactory를 사용한다.
     *
     * */
    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where (    member.username.eq(member.username)
                       .and(member.username.like("Anakin"))
                       ,    member.age.lt(100)            // and 일 경우 콤마로 "," 대신할 수 있다.
                      )
                .fetchOne();

        // Then : 조회 조건과 결과가 같은 지 검증한다.
        assertThat(findMember.getUsername()).isEqualTo("Anakin");
    }


    /**
     *
     *  <결과 조회 - resultFetch()>
     *     - List              : fetch()
     *     - 단건               : fetchOne()
     *     - 처음 한 건 조회     : fetchFirst()
     *     - 페이징에서 사용     : fetchResults()
     *     - Count 쿼리로 변경  : fetchCount()
     *
     * */

    @Test
    public void resultFetch() {

        /**
         * <결과 조회 - fetch()>
         *    - List  : fetch()
         * */
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch()
                ;

        /**
         * <결과 조회 - fetchOne()>
         *    - One   : fetchOne()
         *    - 결과가 없으면 : null
         *    - 결과가 둘 이상이면 ErrorException : com.querydsl.core.NonUniqueResultException
         * */
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.like("Anakin"))
                .fetchOne()
                ;

        /**
         * <결과 조회 - fetchFirst()>
         *    - First : fetchFirst()
         * */
        Member fetchFirst = queryFactory
                .selectFrom(member)
                //.limit(1).fetchOne()
                .fetchFirst()
                ;

        /**
         * <결과 조회 - fetchResults()>
         *    - QueryResults : fetchResults()
         *    - getTotal()   : 전체 카운트 하기
         * */
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
//                .limit(1)            // 페이징을 위해서 첫번째 index 지정
//                .offset(2)          // 페이징을 위해서 페이지 size 지정
                .fetchResults()
                ;

        for (Member fetch1 : fetch) {
            System.out.println("fetch1.getUsername() = " + fetch1.getUsername());
        }

        // 검색 결과 건수 확인
        // 전체 건수를 가져오기 위해서 쿼리를 한 번 더 수행한다.
        results.getTotal();


        /**
         * <결과 조회 - fetchCount()>
         *    - fetchCount : fetchCount()
         * */
        long totalOnce = queryFactory
                .selectFrom(member)
                .fetchCount()
                ;
    }


    /**
     *
     *  <정렬조회 sort()>
     *     - orderBy  : orderBy()
     *     - 1. 나이순으로 내림차순(Desc)
     *     - 2. 이름순으로 오름차순(Asc)
     *     -    단, 조건 2에서 회원이름이 없으면 마지막에 출력 Null Last)
     *
     * */
    @Test
    public void sort() {

        /**
         * <결과 조회 - orderBy()>
         *    - orderBy  : orderBy()
         * */
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch()
                ;
        for (Member fetch1 : result) {
            System.out.println("\tfetch1.getAge()      = " + fetch1.getAge());
            System.out.println("\t\tfetch1.getUsername() = " + fetch1.getUsername());

        }

    }


    /**
     *
     *  <페이징조회 paging()>
     *     - paging  : paging()
     *     - 1. 나이순으로 내림차순(Desc)
     *     - 2. 이름순으로 오름차순(Asc)
     *     -    단, 조건 2에서 회원이름이 없으면 마지막에 출력 Null Last)
     *
     * */
    @Test
    public void paging1() {

        /**
         * <결과 조회 - orderBy()>
         *    - orderBy  : orderBy()
         * */
        List<Member> pageResult = queryFactory
                .selectFrom(member)
                .orderBy(member.username.asc().nullsLast())
                .offset(0)    // 0부터 시작(zero index)
                .limit(2)     // 최대 2건 조회
                .fetch()
                ;
        for (Member fetch1 : pageResult) {
            System.out.println("\tfetch1.getAge()      = " + fetch1.getAge());
            System.out.println("\t\tfetch1.getUsername() = " + fetch1.getUsername());
        }
    }

    /**
     *
     *  <페이징 + 건수 조회 paging2()>
     *     - paging2  : paging2()
     *     - 1. 나이순으로 내림차순(Desc)
     *     - 2. 이름순으로 오름차순(Asc)
     *     -    단, 조건 2에서 회원이름이 없으면 마지막에 출력 Null Last)
     *
     * */
    @Test
    public void paging2() {
        /**
         * <결과 조회 - orderBy()>
         *    - orderBy  : orderBy()
         * */
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.asc().nullsLast())
                .offset(0)    // 0부터 시작(zero index)
                .limit(2)     // 최대 2건 조회
                .fetchResults()
                ;

        assertThat(queryResults.getTotal()).isEqualTo(6);
        assertThat(queryResults.getOffset()).isEqualTo(0);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }



    /**
     *  <그룹(집합)함수>
     *     - Count, Sum, Agv, Max, Min
     * */
    @Test
    public void grpFunction() {
        /**
         * <그룹(집합)함수- Count, Sum, Agv, Max, Min>
         *    - return : Tuple (여러 개의 Type을 같이 Return 할 경우)
         *    - Tuple 보다는 주로 실무에서는 DTO로 사용한다.
         *
         * */
        List<Tuple> result = queryFactory
                .select(
                        member.count()
                        , member.age.sum()
                        , member.age.avg()
                        , member.age.max()
                        , member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        System.out.println("\t\t member.count() = " + tuple.get(member.count()));
        System.out.println("\t\t member.sum()   = " + tuple.get(member.age.sum()));
        System.out.println("\t\t member.avg()   = " + tuple.get(member.age.avg()));
        System.out.println("\t\t member.max()   = " + tuple.get(member.age.max()));
        System.out.println("\t\t member.min()   = " + tuple.get(member.age.min()));

    }

    /**
     *  <groupBy>
     *     - Count, Sum, Agv, Max, Min
     *     - 1. 팀으로 그룹하여 계산하라.
     * */
    @Test
    public void groupBy() {
        /**
         * <groupBy함수- groupBy()>
         *    - return : Tuple (여러 개의 Type을 같이 Return 할 경우)
         *    - Tuple 보다는 주로 실무에서는 DTO로 사용한다.
         * */
        List<Tuple> result = queryFactory
                .select( team.name
                        , member.count()
                        , member.age.min()
                        , member.age.avg()
                        , member.age.max()
                        , member.age.sum()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(50))
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        Tuple teamC = result.get(2);

        System.out.println("\t\t teamA count() = " + teamA.get(member.count()));
        System.out.println("\t\t teamA sum()   = " + teamA.get(member.age.sum()));
        System.out.println("\t\t teamA avg()   = " + teamA.get(member.age.avg()));
        System.out.println("\t\t teamA max()   = " + teamA.get(member.age.max()));
        System.out.println("\t\t teamA min()   = " + teamA.get(member.age.min()));

        System.out.println("\t\t teamB count() = " + teamB.get(member.count()));
        System.out.println("\t\t teamB sum()   = " + teamB.get(member.age.sum()));
        System.out.println("\t\t teamB avg()   = " + teamB.get(member.age.avg()));
        System.out.println("\t\t teamB max()   = " + teamB.get(member.age.max()));
        System.out.println("\t\t teamB min()   = " + teamB.get(member.age.min()));

        System.out.println("\t\t teamC count() = " + teamC.get(member.count()));
        System.out.println("\t\t teamC sum()   = " + teamC.get(member.age.sum()));
        System.out.println("\t\t teamC avg()   = " + teamC.get(member.age.avg()));
        System.out.println("\t\t teamC max()   = " + teamC.get(member.age.max()));
        System.out.println("\t\t teamC min()   = " + teamC.get(member.age.min()));
    }




    /**
     *  <innerJoin>
     *     -
     * */
    @Test
    public void join() {
        /**
         * <innerJoin>
         *    - inner Join
         * */
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("ATEAM"))
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1.getUsername()+"("+member1.getTeam().getName()+")");
        }
    }


    /**
     *  <leftJoin>
     *     -
     * */
    @Test
    public void leftJoin() {
        /**
         * <leftJoin>
         *    - left Join
         * */
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("ATEAM"))
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1.getUsername()+"("+member1.getTeam().getName()+")");
        }
    }



    /**
     *  <thetaJoin>
     *     - 세타조인
     *     - 연관관계가 없어도 조인을 할 수 있다.
     *     - 회원의 이름이 팀이름과 같은 회원을 조회하는 예제
     *     - from 절에 여러 엔티티를 선택해서 세타 조인
     *     - 외부 조인 불가능 다음에 설명할 조인 on을 사용하면 외부 조인 가능
     *     - 세타조인의 조건은{=,≠,≥,≤,>,<} 중 하나가 된다.
     * */
    @Test
    public void thetaJoin() {
        /**
         * <thetaJoin>
         *    - thetaJoin
         *
         * */

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1.getUsername()+"("+member1.getTeam().getName()+")");
        }
    }


    /**
     *  <JoinOn>
     *     - JoinOn1
     *     - 연관관계가 없어도 조인을 할 수 있다.
     *     - ON절을 활용한 조인(JPA 2.1부터 지원)
     *        1. 조인 대상 필터링
     *        2. 연관관계 없는 엔티티 외부 조인
     * */
    @Test
    public void join_on_fintering() {
        /**
         * <JoinOn>
         *    - JoinOn1
         *    - 예) 회원과 팀을 조인하면서, 팀 이름이  teamA인 팀만 조인, 회원은 모두 조회
         *    - JPQL : select m, t,
         *               from Member m left join m.team t
         *                 on t.name = 'teamA'
         *
         *    - 참고 : on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인(inner join)을 사용하면,
         *            where 절에서 필터링 하는 것과 기능이 동일하다. 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때,
         *            내부조인 이면 익숙한 where 절로 해결하고, 정말 외부조인이 필요한 경우에만 이 기능을 사용하자.
         * */
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    /**
     *  <JoinOn>
     *     - JoinOn2
     *     - 연관관계가 없어도 조인을 할 수 있다.
     *     - 회원의 이름이 팀이름과 같은 대상 외부 조인
     * */
    @Test
    public void join_on_relation() {
        /**
         * <JoinOn>
         *    - Left-Outer Join
         *    - 조건에 만족하지 않으면 null 값으로 반환
         * */
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                //.leftJoin(member.team, team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }



    @PersistenceUnit
    EntityManagerFactory emf;
    /**
     *  <fetchJoinNo>
     *     - fetchJoinNo
     * */
    @Test
    public void fetchJoinNo() throws Exception {
        /**
         * <fetchJoinNo>
         * */
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("Anakin"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }


    /**
     *  <fetchJoinUse>
     *     - fetchJoinUse
     *     - 연관관계가 없어도 조인을 할 수 있다.
     * */
    @Test
    public void fetchJoinUse() throws Exception {
        /**
         * <fetchJoinUse>
         * */
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("Anakin"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용 시").isTrue();
    }


    /**
     * <subQuery>
     *     - 서브 쿼리
     *     - JPAExpressions
     *     - Equal
     *     - * 나이가 가장 많은 회원 조회 *
     *
     * */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(224);
    }



    /**
     * <subQuery>
     *     - 서브 쿼리
     *     - JPAExpressions
     *     - Greate Equal
     *     - * 평균나이보다 많은 사람 조회 *
     *
     * */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        // 결과 비교 시 순서가 중요함.
        assertThat(result).extracting("age")
                .containsExactly(120, 130, 224, 125);
    }



    /**
     * <subQuery>
     *     - 서브 쿼리
     *     - JPAExpressions
     *     - In
     *     - * 126살 보다 나이가 많은 사람들 조회 *
     *
     * */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(126))
                ))
                .fetch();

        // 결과 비교 시 순서가 중요함.
        assertThat(result).extracting("age")
                .containsExactly(130, 224);
    }



    /**
     * <selectSubQuery>
     *     - Select절  서브 쿼리
     *     - JPAExpressions
     *
     *     <from절 서브쿼리의 한계>
     *         - JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다.
     *         - 당연히 Querydsl도 지원하지 않는다.
     *         - 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다.
     *         - Querydsl도 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     *
     *     <from 절의 서브쿼리 해결방안>
     *     1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
     *     2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
     *     3. nativeSQL을 사용한다
     *
     * */
    @Test
    public void selectSubQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }
    }


    /**
     *  <Case 문>
     *    - select, 조건절(where), order by에서 사용 가능
     *    - 단순한 조건
     *    - 가급적 CASE문장은 DB로 처리하지 말자
     * */

    @Test
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    /**
     *  <Case 문>
     *    - select, 조건절(where), order by에서 사용 가능
     *    - 복잡한 조건
     *    - CaseBuilder 사용
     *    - 가급적 CASE문장은 DB로 처리하지 말자
     * */

    @Test
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .when(member.age.between(31, 50)).then("31~50살")
                        .when(member.age.between(51, 100)).then("51~100살")
                        .when(member.age.between(101, 150)).then("101~150살")
                        .when(member.age.between(151, 200)).then("151~200살")
                        .otherwise("기타-201살 이상"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    /**
     *  <orderByCase 문>
     *    - select, 조건절(where), order by에서 사용 가능
     *    - orderBy에서 Case 문 함께 사용하기 예제
     *    - NumberExpression 사용
     *    - 가급적 CASE문장은 DB로 처리하지 말자
     * */

    @Test
    public void orderByCase() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }



    /**
     *  <상수, 문자 더하기>
     *    - 상수가 필요하면 Expressions.constant(xxx) 사용
     *    - 참고 : 위와 같이 최적화가 가능하면 SQL에 constant 값을 넘기지 않는다.
     *    - 상수를 더하는 것처럼 최적화가 어려우면 SQL에 constant 값을 넘긴다.
     * */
    @Test
    public void constant() {
        Tuple result = queryFactory
                .select(member.username, Expressions.constant("String A"))
                .from(member)
                .fetchFirst();

        System.out.println("result = " + result);
    }


    /**
     *  <문자 더하기 concat>
     *    - 참고 : member.age.stringValue() 부분이 중요한데,
     *    - 문자가 아닌 다른 타입들은 stringValue()로 문자로 변환할 수 있다.
     *    - 이 방법은 ENUM을 처리할 때도 자주 사용한다.
     * */

    @Test
    public void concat() {
        /**
         * 이름과 나이를 붙여쓰고 싶을때, Type이 다르면 오류가 난다.
         * 이럴 때 concat 사용.
         *     - stringValue()
         *
         * */
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("Anakin"))
                .fetchOne();

        System.out.println("result = " + result);
    }

    /**
     * <중급 문법>
     *     - 프로젝션과 결과 반환 - 기본
     *         .프로젝션: select 대상 지정
     *     - 프로젝션 대상이 하나
     *
     *     - 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
     *
     * */
    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    /**
     * <중급 문법>
     *     - 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
     *     - 튜플 조회
     *     - 프로젝션 대상이 둘 이상일 때 사용
     *        : com.querydsl.core.Tuple
     *
     * */
    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple t : result) {
            System.out.println("username = " + t.get(member.username));
            System.out.println("age      = " + t.get(member.age));
        }
    }



    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     1. 순수 JPA에서 DTO 조회
     *        - 순수 JPA에서 DTO를 조회할 때는 new 명령어를 사용해야함
     *        - DTO의 package이름을 다 적어줘야해서 지저분함
     *        - 생성자 방식만 지원함
     *
     * */
    @Test
    public void findDtoByJPQL() {
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                        " from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }


    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *        - 결과를 DTO 반환할 때 사용
     *        - 다음 3가지 방법 지원
     *
     *        2.1. 프로퍼티 접근
     * */
    @Test
    public void findDtoBySetter() {
        List<MemberDto> resultList = queryFactory
                .select(Projections.bean(MemberDto.class
                    , member.username
                    , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto Setter = " + memberDto);
        }
    }


    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *        - 결과를 DTO 반환할 때 사용
     *        - 다음 3가지 방법 지원
     *
     *        2.2. 필드 직접 접근
     * */
    @Test
    public void findDtoByField() {
        List<MemberDto> resultList = queryFactory
                .select(Projections.fields(MemberDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto fields= " + memberDto);
        }
    }


    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *        - 결과를 DTO 반환할 때 사용
     *        - 다음 3가지 방법 지원
     *
     *        2.3. 생성자 사용
     * */
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> resultList = queryFactory
                .select(Projections.constructor(MemberDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto Constructor= " + memberDto);
        }
    }



    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *        - 결과를 DTO 반환할 때 사용
     *        - 다음 3가지 방법 지원
     *
     *        2.4.1 별칭이 다를 때(fields)
     * */
    @Test
    public void finUserDto() {
        List<UserDto> resultList = queryFactory
                .select(Projections.fields(UserDto.class
                        , member.username.as("name")
                        , member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : resultList) {
            System.out.println("userDto Constructor = " + userDto);
        }
    }



    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *
     *        2.4.2 결과가 없을 때...
     *            : ExpressionUtils
     * */
    @Test
    public void finUserDtoDiffer() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();

        for (UserDto userDto : fetch) {
            System.out.println("userDto ExpressionUtils = " + userDto);
        }
    }


    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     2. Querydsl 빈 생성(Bean population)
     *        - 결과를 DTO 반환할 때 사용
     *        - 다음 3가지 방법 지원
     *
     *        2.3. 생성자 사용
     *            단점 : 컬럼을 추가 시 컴파일시에는 오류가 나지 않고, 실행 시 오류가 남.
     *
     * */
    @Test
    public void findDtoByUserConstructor() {
        List<UserDto> resultList = queryFactory
                .select(Projections.constructor(UserDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : resultList) {
            System.out.println("userDto Constructor= " + userDto);
        }
    }



    /**
     * <프로젝션과 결과 반환 - DTO 조회>
     *     - 제일 깔끔한 해결책
     *     - 생성자 + @QueryProjection
     *     - Gradle에서 compileQuerydls 실행해서 : ./gradlew compileQuerydsl
     *     - DTO까지 Q파일로 생성되도록 하는 옵션   : QMemberDto 생성 확인
     *
     * */
    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto Query Projection = " + memberDto);
        }
    }

    /**
     * <프로젝션 정리>
     *     0. 프로젝션이란? select 대상을 지정하는 것...
     *
     *     1. 단 하나의 컬럼만 조회할 경우    : 타입 지정 후 반환
     *        - List<String> result =
     *
     *     2. 결과가 두개의 컬럼 이상일 경우  : Tuple 이나 DTO로 조회
     *        2.1 튜플
     *           - List<Tuple> result =     : Tuple은 가급적 Repository 계층 안에서만 사용할 것.
     *
     *        2.2 DTO 는 3가지 방법이 있다.
     *           2.2.1 프로퍼티 접근 (bean)
     *               : List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class,
     *
     *           2.2.2 필드 직접 접근 (fields)
     *               : List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class,
     *
     *           2.2.3 생성자 사용 (constructor)
     *               2.2.3.1
     *                   : List<MemberDto> result = queryFactory.select(Projections.constructor(MemberDto.class,
     *
     *               2.2.3.2 생성자 + @QueryProjection 기능도 추가로 제공된다.
     *                   : List<MemberDto> result = queryFactory.select(new QMemberDto(member.username, member.age)).from(member).fetch();
     *                   : 이 방법은 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법이다.
     *                   : 다만 DTO에 QueryDSL 어노테이션을 유지해야 하는 점과 DTO까지 "Q파일"을 생성해야 하는 단점이 있다.
     *
     * */





    /**
     * <동적 쿼리 (Dynamic Query)>
     *     - 동적 쿼리를 해결하는 두가지 방식
     *       1. BooleanBuilder
     *       //2. Where 다중 파라미터 사용
     *
     * */
    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "Anakin";
        Integer ageParam = 28;
        List<Member> result = searchMember1(usernameParam, ageParam);
        //List<Member> result = searchMember1(usernameParam, null);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    // 파라미터의 값이 null 여부에 따라서 쿼리가 동적으로 바뀐다.
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }



    /**
     * <동적 쿼리 (Dynamic Query)>
     *     - 동적 쿼리를 해결하는 두가지 방식
     *       //1. BooleanBuilder
     *       2. Where 다중 파라미터 사용  (강사 추천 방식)
     *
     * */
    @Test
    public void dynamicQuery_whereParam() throws Exception {
        String usernameParam = "Anakin";
        Integer ageParam = 28;
//        List<Member> result = searchMember2(usernameParam, ageParam);
        List<Member> result = searchMember2(usernameParam, null);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    // 파라미터의 값이 null 여부에 따라서 쿼리가 동적으로 바뀐다.
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where( null
                      , usernameEq(usernameCond)  // usernameCond 값이 null이면 조건이 무시된다.
                      , ageEq(ageCond)            // ageCond 값이 null이면 조건이 무시된다.
                )
//                .where(allEq(usernameCond,ageCond)) // 조건을 조합 가능
                .fetch();
    }
    private BooleanExpression usernameEq(String usernameCond) {
        // 간단한 조건은 3항연산자 사용한다.
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // 조합 가능
    // null 체크는 주의해서 처리해야함
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    /**
     * <수정 벌크 연산>
     *     - 쿼리 한번으로 대량 데이터 수정
     *     - 변경감지 (Dirty Check)
     *
     *     - 벌크 연산은 주의해야 할 점.
     *        : 벌크 연산은 DB를 바로 바꾸기 때문에,
     *          영속성 컨텍스트를 변경하지 않는다.
     *
     * */
    @Test
    public void bulkUpdate() {

        // 쿼리 한번으로 대량 데이터 수정
        // 25살 이하의 이름을 "파다완"으로 바꾼다.
        long count = queryFactory
                .update(member)
                .set(member.username, member.username.stringValue().concat(" (파다완)"))
                .where(member.age.lt(25))
                .execute();


        // 모든 기존 나이에 1 더하기
        // 100살이 넘으면 "마스터"로 부른다.
        long updateCount = queryFactory
                .update(member)
                .set(member.age, member.age.add(2))                 // 더하기 2
                .set(member.age, member.age.subtract(1))       // 빼기 1
                .set(member.age, member.age.multiply(100))     // 곱하기 100
                .set(member.age, member.age.divide(10))        // 나누기 10
                .set(member.username, member.username.stringValue().concat(" (마스터)"))  // 문자 합치기
                .where( member.age.gt(100)
                      , member.age.lt(300)
                )
                .execute();

        // 벌크 연산 후 영속성 컨텍스트 확인하기.
        List<Member> result = queryFactory.selectFrom(member).fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }



    /**
     * <삭제 벌크 연산>
     *     - 쿼리 한번으로 대량 데이터 수정
     *     - 변경감지 (Dirty Check)
     *
     *     - 벌크 연산은 주의해야 할 점.
     *        : 벌크 연산은 DB를 바로 바꾸기 때문에,
     *          영속성 컨텍스트를 변경하지 않는다.
     *
     * */
    @Test
    public void bulkDelete() {

        // 50세 이상은 삭제하기.
        long deleteCount = queryFactory
                .delete(member)
                .where(member.age.gt(50))
                .execute();

        // 벌크 연산 후 영속성 컨텍스트 확인하기.
        List<Member> result = queryFactory.selectFrom(member).fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }



    /**
     * <SQL 함수>
     *     - replace 함수 (문자열 대체)
     *     - 함수명 확인
     *        : Ctrl + N = H2Dialect.java
     * */
    @Test
    public void sqlFunction() {
        String result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "ATEAM", "team_a"))
                .from(member)
                .fetchFirst();
        System.out.println("result = " + result);
    }

    /**
     *   <SQL 함수>
     *     - upper함수 (대문자변환)로 조회하기.
     *     - 함수명 확인
     *        : Ctrl + N = H2Dialect.java
     *
     * */
    @Test
    public void sqlFunction2() {
        String result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('upper', {0})",
                //.where(member.username.eq(Expressions.stringTemplate("function('lower', {0})",
                        member.username)))
                .fetchFirst();
        System.out.println("\t\t Result = " + result);
    }


}