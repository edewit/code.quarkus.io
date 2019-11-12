package io.quarkus.code.services

import io.quarkus.code.model.AccessToken
import io.quarkus.code.model.TokenParameter
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.kohsuke.github.GHCreateRepositoryBuilder
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHubBuilder
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.nio.file.Path
import java.util.Objects.requireNonNull
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
open class GitHubService {
    @Inject
    lateinit var configManager: CodeQuarkusConfigManager

    @Throws(UncheckedIOException::class)
    open fun createRepository(token: String, repositoryName: String): Pair<String, String> {
        val newlyCreatedRepo: GHRepository
        try {
            val gitHub = GitHubBuilder().withOAuthToken(token).build()
            val repositoryBuilder: GHCreateRepositoryBuilder = gitHub.createRepository(repositoryName)
            newlyCreatedRepo = repositoryBuilder
                    .description("Generated by code.quarkus.io")
                    .create()
        } catch (e: IOException) {
            throw UncheckedIOException(String.format("Could not create GitHub repository named '%s'", repositoryName), e)
        }

        return Pair(newlyCreatedRepo.ownerName, newlyCreatedRepo.httpTransportUrl)
    }

    open fun push(ownerName: String, token: String, httpTransportUrl: String, path: Path) {
        requireNonNull(httpTransportUrl, "httpTransportUrl must not be null.")
        requireNonNull(ownerName, "ownerName must not be null.")
        requireNonNull(path, "path must not be null.")

        try {
            Git.init().setDirectory(path.toFile()).call().use { repo ->
                repo.add().addFilepattern(".").call()
                repo.commit().setMessage("Initial commit")
                        .setAuthor("quarkusio", "no-reply@quarkus.io")
                        .setCommitter("quarkusio", "no-reply@quarkus.io")
                        .setSign(false)
                        .call()

                val pushCommand = repo.push()
                pushCommand.remote = httpTransportUrl
                pushCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(ownerName, token))
                pushCommand.call()
            }
        } catch (e: GitAPIException) {
            throw IllegalStateException("An error occurred while pushing to the git repo", e)
        }

    }

    open fun fetchAccessToken(code: String, state: String): AccessToken {
        val gitHubOAuthClient = RestClientBuilder.newBuilder()
                .baseUri(URI("https://github.com/"))
                .build(GitHubOAuthClient::class.java)

        return AccessToken(gitHubOAuthClient.getAccessToken(TokenParameter(configManager.clientId, configManager.clientSecret, code, state)))
    }
}