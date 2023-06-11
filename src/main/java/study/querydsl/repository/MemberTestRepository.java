package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.springframework.util.StringUtils.isEmpty;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition,
                                              Pageable pageable) {
        JPAQuery<Member> query =
                selectFrom(member)
                .leftJoin(member.team, team)
                .where( usernameEq(condition.getUsername())
                      , teamNameEq(condition.getTeamName())   // 콤마(,)는 and 와 같음
                      , ageGoe(condition.getAgeGoe())
                      , ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    /**
     * Simple Paging 기능
     *
     * */
    public Page<Member> applyPagination( MemberSearchCondition condition
                                       , Pageable pageable) {
        return applyPagination(pageable
                , contentQuery -> contentQuery
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where( usernameEq(condition.getUsername())
                      , teamNameEq(condition.getTeamName())  // 콤마(,)는 and 와 같음
                      , ageGoe(condition.getAgeGoe())
                      , ageLoe(condition.getAgeLoe())
                )
                .orderBy(member.username.desc())
        );
    }

    /**
     * Complex Paging 기능
     *     - Count 쿼리를 별도로 분리하기.
     *
     * */
    public Page<Member> applyPagination2 ( MemberSearchCondition condition
                                         , Pageable pageable) {
        return applyPagination(pageable, contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where( usernameEq(condition.getUsername())
                              , teamNameEq(condition.getTeamName())    // 콤마(,)는 and 와 같음
                              , ageGoe(condition.getAgeGoe())
                              , ageLoe(condition.getAgeLoe()))
                        .orderBy(member.username.desc())
                      , countQuery -> countQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where( usernameEq(condition.getUsername())
                              , teamNameEq(condition.getTeamName())     // 콤마(,)는 and 와 같음
                              , ageGoe(condition.getAgeGoe())
                              , ageLoe(condition.getAgeLoe())
                        )
                        .orderBy(member.username.desc())
        );
    }

    /**
     *  <회원명 입력조건 null 검증>
     * */
    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }

    /**
     *  <팀명 입력조건 null 검증>
     * */
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    /**
     *  <From Value 입력조건 null 검증>
     * */
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    /**
     *  <To Value 입력조건 null 검증>
     * */
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
