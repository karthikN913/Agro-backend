package com.agrosystem.repository;

import com.agrosystem.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Fetch the full bidirectional chat history between two users, ordered by timestamp.
     * The Spring Data derived-query method name was generating incorrect SQL because
     * the OR clause wasn't properly grouped — (A AND B) OR (C AND D) was being parsed
     * as A AND (B OR C) AND D. Using an explicit @Query fixes this.
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :uid1 AND m.receiver.id = :uid2) OR " +
           "(m.sender.id = :uid2 AND m.receiver.id = :uid1) " +
           "ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("uid1") Long uid1, @Param("uid2") Long uid2);
}
