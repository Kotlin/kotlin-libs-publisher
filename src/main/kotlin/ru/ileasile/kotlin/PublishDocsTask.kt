package ru.ileasile.kotlin

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

abstract class PublishDocsTask: DefaultTask() {
    @Input
    val docsRepoUrl: Property<String> = project.objects.property()

    @Input
    val username: Property<String> = project.objects.property()

    @Input
    val email: Property<String> = project.objects.property()

    @Input
    val branchName: Property<String> = project.objects.property()
}
