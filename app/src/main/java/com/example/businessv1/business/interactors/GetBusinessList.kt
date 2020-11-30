package com.example.businessv1.business.interactors

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.businessv1.business.data.BusinessPagingSource
import com.example.businessv1.business.data.cache.CacheDataSource
import com.example.businessv1.business.data.network.NetworkDataSource
import com.example.businessv1.business.domain.model.Business
import com.example.businessv1.business.domain.model.BusinessDetails
import com.example.businessv1.business.domain.model.BusinessFavorite
import com.example.businessv1.business.domain.state.DataState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class GetBusinessList
constructor(
    private val cacheDataSource: CacheDataSource,
    private val networkDataSource: NetworkDataSource
){


    /**
     * Show loading
     * Get blogs from network
     * Insert blogs into cache
     * Show List<Blog>
     */

    fun getSearchResults(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { BusinessPagingSource(networkDataSource,cacheDataSource, query) }
        ).liveData

    suspend fun getBusiness(query:String,offset:Int,limit:Int):Flow<DataState<List<Business>>> = flow {
        emit(DataState.Loading)
        delay(1000)
        try {
            val networkBusiness = networkDataSource.getList(query,limit,offset).businesses
            if (offset == 0) {
                cacheDataSource.deleteAll()
                cacheDataSource.insertList(networkBusiness)
                emit(DataState.Success(networkBusiness))
            }else{
                emit(DataState.Success(networkBusiness))
            }

        }catch (e : Throwable){
            val cachedBlogs = cacheDataSource.get()
            emit(DataState.Success(cachedBlogs))
        }

    }

    suspend fun getBusinessDetails(busniess_id:String):Flow<DataState<BusinessDetails>> = flow {
         emit(DataState.Loading)
        try {
            val networkBusinessDetails = networkDataSource.getDetails(busniess_id)
            emit(DataState.Success(networkBusinessDetails))

        }catch (e : Exception){
            emit(DataState.Error(e))
        }
    }



    suspend fun insertFavorite(business:Business){
        cacheDataSource.insertFavorite(business)
    }


    suspend fun deleteFavorite(business:Business){
        cacheDataSource.deleteBusinessFav(business.id)
    }



    suspend fun getAllFavorite():List<Business>{
        return cacheDataSource.getFav()
    }




}