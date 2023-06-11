package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl
//        extends QuerydslRepositorySupport
        implements MemberRepositoryCustom
{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }

    @Override
    public List<MemberTeamDto> search (MemberSearchCondition condition) {

//        return   from(member)
//                .leftJoin(member.team, team)
//                .where(null
//                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
//                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
//                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
//                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
//                )
//                .select(new QMemberTeamDto(
//                        member.id
//                        , member.username
//                        , member.age
//                        , team.id
//                        , team.name
//                ))
//                .fetch();

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


    /**
     *   <심플 페이징처리>
     *       - 간단한 페이지 예제
     *
     * */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id
                        , member.username
                        , member.age
                        , team.id
                        , team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();                               // <= 이 부분이 중요함.

        List<MemberTeamDto> content = results.getResults();

        // 전체 건수
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);

    }


    /**
     *   <복잡한 페이징처리>
     *       - 복잡한 페이지 예제
     *       - 내용과 전체 건 수를 분리해는 기술
     *       - 성능최적화를 위해서 카운트는 필요없을 경우 유용함.
     *
     * */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id
                        , member.username
                        , member.age
                        , team.id
                        , team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();                                      // <= 전체 건수를 따로 구현하고자 할때...

        // 카운트가 필요 없을 때
        // 카운트 쿼리를 최적화 하고 싶을 때...
        // 전체 건수를 직접, 따로 구현하고자 할 때...
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                )
                .fetchCount()
                ;

        return new PageImpl<>(content, pageable, total);
    }




    /**
     *   <페이징 최적화 처리>
     *       - 마지막 페이지가 자동 인식되면,
     *       - 카운트 쿼리를 수행되지 않으므로 최적화 됨.
     *
     * */
    @Override
    public Page<MemberTeamDto> searchPageCount(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id
                        , member.username
                        , member.age
                        , team.id
                        , team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();                                      // <= 전체 건수를 따로 구현하고자 할때...

        // 카운트가 필요 없을 때
        // 마지막 페이지 등은 카운트 쿼리를 수행할 필요 없으므로, 최적화 됨.
       JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(null
                        , usernameEq(condition.getUsername())  // 입력이  null 값이면 조건이 무시된다.
                        , teamNameEq(condition.getTeamName())  // 입력이  null 값이면 조건이 무시된다.
                        , ageGoe(condition.getAgeGoe())        // 입력이  null 값이면 조건이 무시된다.
                        , ageLoe(condition.getAgeLoe())        // 입력이  null 값이면 조건이 무시된다.
                );

        //return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

}
