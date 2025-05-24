package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.NotificationRecipientEntity;
import org.socius.sociuswebbackend.model.entities.NotificationRecipientId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipientEntity, NotificationRecipientId> {

}
