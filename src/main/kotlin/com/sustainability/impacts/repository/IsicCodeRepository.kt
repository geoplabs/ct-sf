package com.sustainability.impacts.repository

import com.sustainability.impacts.model.IsicCode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface IsicCodeRepository : MongoRepository<IsicCode, String> {
    fun findByCode(code: String): IsicCode?

    @Query("{'title': {\$regex: ?0, \$options: 'i'}}")
    fun searchByTitle(searchTerm: String): List<IsicCode>

    fun findBySection(section: String): List<IsicCode>

    fun findByDivision(division: String): List<IsicCode>

    fun findByGroup(group: String): List<IsicCode>

    fun findByRevision(revision: String): List<IsicCode>
} 
