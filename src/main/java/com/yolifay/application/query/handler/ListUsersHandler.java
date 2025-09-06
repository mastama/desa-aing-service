package com.yolifay.application.query.handler;

import com.yolifay.application.query.ListUsersQuery;
import com.yolifay.application.shared.paging.PagedResult;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import com.yolifay.infrastructure.adapter.in.web.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListUsersHandler {
    private final UserRepositoryPortOut userRepo;

    public PagedResult<UserSummary> handleListUser(ListUsersQuery query) {
        log.info("handle list users");
        var res = userRepo.search(query.q(), query.page(), query.size());
        var content = res.content().stream().map(u ->
                new UserSummary(
                        u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getStatus(), u.getRoles()
                )).toList();

        log.info("Listed users with query");
        return new PagedResult<>(content, res.page(), res.size(), res.totalElements(), res.totalPages());
    }
}
