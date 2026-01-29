package com.devtiro.realestate.repositories;

import com.devtiro.realestate.domain.entities.PropertyListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyListingRepository extends ElasticsearchRepository<PropertyListing, String> {
    @Override
    Page<PropertyListing> findAll(Pageable pageable);

    Page<PropertyListing> findAllByAgentId(String agentId, Pageable pageable);
}
