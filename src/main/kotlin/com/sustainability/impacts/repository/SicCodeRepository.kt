package com.sustainability.impacts.repository

import com.sustainability.impacts.model.SicCode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SicCodeRepository : MongoRepository<SicCode, String> {
    fun findByCode(code: String): SicCode?

    @Query("{'title': {\$regex: ?0, \$options: 'i'}}")
    fun searchByTitle(searchTerm: String): List<SicCode>

    fun findByDivision(division: String): List<SicCode>

    fun findByMajorGroup(majorGroup: String): List<SicCode>
} 
