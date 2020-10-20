package com.example.facilityprojects.model

interface CartInterface {
    fun saveProduct(idProduct: String, idUser: String)
    fun deleteProduct(idProduct: String, idUser: String)
}