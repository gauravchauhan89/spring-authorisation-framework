package com.github.gauravchauhan89.framework.authorisation.impl;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<RoleDTO, String> {
}
