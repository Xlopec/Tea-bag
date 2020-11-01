@file:Suppress("FunctionName")

package com.max.reader.app.env.storage.local

import android.content.Context
import com.mongodb.client.MongoCollection
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService
import org.bson.Document

interface HasMongoCollection {
    val collection: MongoCollection<Document>
}

fun MongoCollection(
    context: Context
): HasMongoCollection = object : HasMongoCollection {

    override val collection: MongoCollection<Document> by lazy {
        Stitch.initialize(context)

        val client = Stitch.initializeAppClient(context.packageName)

        client.getServiceClient(LocalMongoDbService.clientFactory)
            .getDatabase("app")
            .getCollection("favorite")
    }

}