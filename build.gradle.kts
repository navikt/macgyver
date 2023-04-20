import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val coroutinesVersion = "1.6.4"
val jacksonVersion = "2.14.2"
val kluentVersion = "1.72"
val ktorVersion = "2.3.0"
val logbackVersion = "1.4.6"
val logstashEncoderVersion = "7.3"
val prometheusVersion = "0.16.0"
val nimbusjosejwtVersion = "9.31"
val hikariVersion = "5.0.1"
val jaxbBasicAntVersion = "1.11.1"
val javaxAnnotationApiVersion = "1.3.2"
val jaxwsToolsVersion = "2.3.1"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val javaxJaxwsApiVersion = "2.3.1"
val jaxbApiVersion = "2.4.0-b180830.0359"
val javaxActivationVersion = "1.1.1"
val mockkVersion = "1.13.5"
val smCommonVersion = "1.dec4861"
val sykmeldingVersion = "2019.09.09-08-50-693492ddc1d3f98e70c1638c94dcb95a66036d12"
val fellesformatVersion = "2019.07.30-12-26-5c924ef4f04022bbb850aaf299eb8e4464c1ca6a"
val kithHodemeldingVersion = "2019.09.09-08-50-693492ddc1d3f98e70c1638c94dcb95a66036d12"
val javaTimeAdapterVersion = "1.1.3"
val postgresVersion = "42.6.0"
val confluentVersion = "6.2.2"
val swaggerUiVersion = "4.18.2"
val kotlinVersion = "1.8.20"
val kotestVersion = "5.4.1"
val googlePostgresVersion = "1.11.1"
val junitVersion = "5.9.2"
val nimbusdsVersion = "9.31"
val commonsCodecVersion = "1.15"


plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jmailen.kotlinter") version "3.14.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.hidetake.swagger.generator") version "2.19.2" apply true
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfosm-common")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    // override transient version 1.11 from io.ktor:ktor-client-apache due to security vulnerability
    // https://devhub.checkmarx.com/cve-details/Cxeb68d52e-5509/
    implementation("commons-codec:commons-codec:$commonsCodecVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")


    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("no.nav.helse:syfosm-common-models:$smCommonVersion")
    implementation("no.nav.helse.xml:sm2013:$sykmeldingVersion")
    implementation("no.nav.helse.xml:xmlfellesformat:$fellesformatVersion")
    implementation("no.nav.helse.xml:kith-hodemelding:$kithHodemeldingVersion")
    implementation("no.nav.helse:syfosm-common-kafka:$smCommonVersion")
    implementation("no.nav.helse:syfosm-common-diagnosis-codes:$smCommonVersion")
    implementation("com.migesok:jaxb-java-time-adapters:$javaTimeAdapterVersion")

    implementation("javax.xml.ws:jaxws-api:$javaxJaxwsApiVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("javax.activation:activation:$javaxActivationVersion")
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }

    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusdsVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.google.cloud.sql:postgres-socket-factory:$googlePostgresVersion") {
        exclude(group = "commons-codec", module = "commons-codec")
    }
    swaggerUI("org.webjars:swagger-ui:$swaggerUiVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("com.nimbusds:nimbus-jose-jwt:$nimbusjosejwtVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

swaggerSources {
    create("macgyver").apply {
        setInputFile(file("api/oas3/macgyver-api.yaml"))
    }
}


tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.BootstrapKt"
        dependsOn("generateSwaggerUI")
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        dependsOn("generateSwaggerUI", "generateSwaggerUIMacgyver")
    }

    withType<org.hidetake.gradle.swagger.generator.GenerateSwaggerUI> {
        outputDir = File(buildDir.path + "/resources/main/api")
    }

    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        transform(com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        dependsOn("generateSwaggerUI")
    }

    withType<Test> {
        useJUnitPlatform {
        }
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    "check" {
        dependsOn("formatKotlin", "generateSwaggerUI")
    }
}
