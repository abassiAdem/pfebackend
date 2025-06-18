package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.enmus.NotificationMethod;
import com.example.demo.enmus.NotificationStatus;
import com.example.demo.enmus.NotificationType;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Notification;
@Repository
public interface NotificatonRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.isRead = false")
    int markAllUnreadAsRead();
	List<Notification> findByResponderIdAndIsReadFalseOrderByCreatedAtDesc(Long responderId);
	boolean existsByDemandeAndType(Demande demande, NotificationType type);
    List<Notification> findByResponderId(Long responderId);
    List<Notification> findByRequesterId(Long requesterId);
    List<Notification> findByResponderIdOrderByCreatedAtDesc(Long responderId);
    List<Notification> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<Notification> findByStatut(NotificationStatus status);
    List<Notification> findByType(NotificationType type);
    List<Notification> findByDemandeId(Long demandeId);
    List<Notification> findByIsReadFalse();
    @Query("SELECT n FROM Notification n WHERE n.responder.id = :responderId AND n.method = :method ORDER BY n.createdAt DESC")
    List<Notification> findByResponderIdAndMethod(Long responderId,NotificationMethod method);
}
