package com.sustainability.impacts.repository

import com.sustainability.impacts.model.IndustryCrosswalk
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IndustryCrosswalkRepository : MongoRepository<IndustryCrosswalk, String> {
    fun findByNaicsCode(naicsCode: String): List<IndustryCrosswalk>

    fun findBySicCode(sicCode: String): List<IndustryCrosswalk>

    fun findByIsicCode(isicCode: String): List<IndustryCrosswalk>

    fun findByNaicsCodeAndNaicsYear(naicsCode: String, naicsYear: String): List<IndustryCrosswalk>

    fun findBySicCodeAndSicYear(sicCode: String, sicYear: String): List<IndustryCrosswalk>

    fun findByIsicCodeAndIsicRevision(isicCode: String, isicRevision: String): List<IndustryCrosswalk>
} 
