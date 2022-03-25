
import gradle.kotlin.dsl.accessors._2cce830b43201e290b93c3ed1e38ead2.signing

plugins {
    signing
}

signing {
    useInMemoryPgpKeys(signingKey, signingPassword)

    val publishing: PublishingExtension by project

    sign(publishing.publications)
}
