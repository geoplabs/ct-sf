package com.sustainability.impacts.repository

import com.sustainability.impacts.model.NaicsCode
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NaicsCodeRepository : MongoRepository<NaicsCode, String> {

    /**
     * Find NAICS codes by parent code
     */
    fun findByParentCode(parentCode: String?): List<NaicsCode>

    /**
     * Find NAICS codes by level
     */
    fun findByLevel(level: Int): List<NaicsCode>

    /**
     * Find a specific NAICS code by code value
     */
    fun findByCode(code: String): NaicsCode?

    /**
     * Search NAICS codes by title containing the search term
     */
    @Query("{ 'title': { \$regex: ?0, \$options: 'i' } }")
    fun searchByTitle(searchTerm: String): List<NaicsCode>

    /**
     * Search NAICS codes by either code or title
     */
    @Query("{ \$or: [ { 'code': { \$regex: ?0, \$options: 'i' } }, { 'title': { \$regex: ?0, \$options: 'i' } } ] }")
    fun searchByCodeOrTitle(searchTerm: String): List<NaicsCode>

    /**
     * Find NAICS codes where the code starts with the given prefix
     */
    @Query("{ 'code': { \$regex: '^?0', \$options: 'i' } }")
    fun findByCodeStartingWith(codePrefix: String): List<NaicsCode>
} 
