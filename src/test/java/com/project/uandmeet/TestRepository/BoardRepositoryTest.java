package com.project.uandmeet.TestRepository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.project.uandmeet.model.Board;
import com.project.uandmeet.repository.BoardRepository;

@DataJpaTest
public class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    public void testSearchByBoardTypeAndTitleContainingOrContentContaining() {
        String boardType = "matching";
        String query = "확인";
        PageRequest pageable = PageRequest.of(0, 10);

        Page<Board> result = boardRepository.searchByBoardTypeAndTitleContainingOrContentContaining(boardType, query, pageable);

        assertNotNull(result);
        assertTrue(result.hasContent());
    }
}
