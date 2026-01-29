package com.devtiro.realestate.repositories;

import com.devtiro.realestate.domain.entities.PropertyViewing;
import com.devtiro.realestate.domain.entities.ViewingStatus;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyViewingRepository extends ElasticsearchRepository<PropertyViewing, String> {

    Optional<PropertyViewing> findById(String id);

    List<PropertyViewing> findAllByUserId(String userId);

    List<PropertyViewing> findAllByUserIdAndStatus(String userId, ViewingStatus status);

    List<PropertyViewing> findAllByAgentIdAndStatus(String agentId, ViewingStatus status);

    List<PropertyViewing> findAllByAgentId(String agentId);

    List<PropertyViewing> findAllByPropertyListingId(String propertyListingId);

}
