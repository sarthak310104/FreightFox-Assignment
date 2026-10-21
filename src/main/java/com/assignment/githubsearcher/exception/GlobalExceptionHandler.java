package com.assignment.githubsearcher.exception;

import com.assignment.githubsearcher.dto.SortOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 400: bad input from the client ---------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidBody(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleInvalidParameters(HandlerMethodValidationException ex) {
        String detail = ex.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        if (ex.getRequiredType() == SortOption.class) {
            detail += ". Allowed values: stars, forks, updated";
        }
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String detail = cause instanceof IllegalArgumentException && cause.getMessage() != null
                ? cause.getMessage()
                : "Malformed request body or invalid field value";
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    // ---------- GitHub problems ----------

    @ExceptionHandler(InvalidSearchQueryException.class)
    public ProblemDetail handleInvalidQuery(InvalidSearchQueryException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(GithubRateLimitException.class)
    public ResponseEntity<ProblemDetail> handleRateLimit(GithubRateLimitException ex) {
        ProblemDetail problem = problem(HttpStatus.TOO_MANY_REQUESTS,
                "GitHub API rate limit exceeded. Try again later or configure GITHUB_TOKEN.");
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);

        if (ex.getResetAt() != null) {
            long seconds = Math.max(0, Duration.between(Instant.now(), ex.getResetAt()).getSeconds());
            response.header(HttpHeaders.RETRY_AFTER, String.valueOf(seconds));
            problem.setProperty("resetAt", ex.getResetAt());
        }
        return response.body(problem);
    }

    @ExceptionHandler(GithubApiException.class)
    public ProblemDetail handleGithubApi(GithubApiException ex) {
        log.warn("GitHub API error: {}", ex.getMessage(), ex);
        return problem(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // ---------- Standard Spring errors (404, 405, 415) ----------

    @ExceptionHandler({
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ProblemDetail> handleSpringMvcErrors(Exception ex) {
        ErrorResponse errorResponse = (ErrorResponse) ex;
        return ResponseEntity.status(errorResponse.getStatusCode()).headers(errorResponse.getHeaders()).body(errorResponse.getBody());
    }

    // ---------- 500: anything unexpected ----------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}