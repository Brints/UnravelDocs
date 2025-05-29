package com.extractor.unraveldocs.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListData {
    private List<UserSummary> users;
    private int totalUsers;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
