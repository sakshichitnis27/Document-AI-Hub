# Contributing to Document AI Hub

Thank you for considering contributing to Document AI Hub! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please be respectful and constructive in all interactions.

### Our Standards

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

1. **Fork the Repository**
   ```bash
   git clone https://github.com/yourusername/Document-AI-Hub.git
   cd Document-AI-Hub
   ```

2. **Set Up Development Environment**
   - Install Java 17
   - Install MySQL 8.0
   - Set up environment variables (see README.md)

3. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Process

### Branch Naming Convention

- `feature/` - New features (e.g., `feature/add-ocr-support`)
- `bugfix/` - Bug fixes (e.g., `bugfix/fix-upload-validation`)
- `hotfix/` - Critical production fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions or modifications

### Development Workflow

1. Create a feature branch from `main`
2. Make your changes
3. Write/update tests
4. Ensure all tests pass
5. Update documentation
6. Commit your changes
7. Push to your fork
8. Create a Pull Request

## Coding Standards

### Java Code Style

- **Naming Conventions**
  - Classes: `PascalCase` (e.g., `DocumentService`)
  - Methods: `camelCase` (e.g., `extractText()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_FILE_SIZE`)
  - Packages: lowercase (e.g., `com.chitnis.document_management_app.service`)

- **Package Organization**
  ```
  com.chitnis.document_management_app
  ├── config/          # Configuration classes
  ├── controller/      # REST controllers
  ├── dto/            # Data transfer objects
  ├── entity/         # JPA entities
  ├── repository/     # Data repositories
  ├── service/        # Business logic
  ├── security/       # Security components
  ├── util/           # Utility classes
  └── ai/             # AI integrations
  ```

- **Code Quality**
  - Keep methods small and focused (Single Responsibility Principle)
  - Use meaningful variable and method names
  - Avoid magic numbers; use named constants
  - Maximum line length: 120 characters
  - Use proper exception handling

### Spring Boot Best Practices

- Use constructor injection over field injection
- Annotate service classes with `@Service`
- Annotate repositories with `@Repository`
- Use `@Transactional` for database transactions
- Properly configure `@RestController` and mappings

### Documentation

- Add JavaDoc comments for public methods and classes
- Include parameter descriptions and return value documentation
- Document complex logic with inline comments
- Update README.md for new features

Example:
```java
/**
 * Extracts text content from a PDF document.
 *
 * @param documentId the unique identifier of the document
 * @return the extracted text content
 * @throws EntityNotFoundException if the document is not found
 * @throws IOException if text extraction fails
 */
public String extractText(Long documentId) throws IOException {
    // Implementation
}
```

## Commit Guidelines

### Commit Message Format

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```bash
feat(document): add support for Word document processing

- Implement DOCX text extraction
- Add Word MIME type validation
- Update document service with Word support

Closes #123
```

```bash
fix(auth): resolve JWT token expiration issue

The token expiration was not being properly validated,
causing security vulnerabilities.

Fixes #456
```

## Pull Request Process

### Before Submitting

1. **Update Your Branch**
   ```bash
   git checkout main
   git pull upstream main
   git checkout feature/your-feature
   git rebase main
   ```

2. **Run Tests**
   ```bash
   mvn clean test
   ```

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Check Code Quality**
   - Ensure no compiler warnings
   - Fix any linting issues
   - Verify all tests pass

### Submitting a Pull Request

1. Push your branch to your fork
2. Create a Pull Request against the `main` branch
3. Fill out the PR template completely
4. Link any related issues
5. Wait for review

### PR Review Process

- At least one approving review is required
- All CI checks must pass
- Address any requested changes
- Keep the PR focused and small when possible

## Testing Guidelines

### Unit Tests

- Write unit tests for all new functionality
- Aim for at least 80% code coverage
- Use JUnit 5 and Mockito
- Test edge cases and error conditions

Example:
```java
@Test
void testUploadDocument_Success() {
    // Arrange
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.pdf");

    // Act
    Document result = documentService.uploadDocument(file);

    // Assert
    assertNotNull(result);
    assertEquals("test.pdf", result.getOriginalFileName());
}
```

### Integration Tests

- Test complete workflows
- Use `@SpringBootTest` for integration tests
- Test database interactions
- Verify API endpoints

### Test Naming Convention

```java
// Pattern: methodName_scenario_expectedBehavior
void uploadDocument_withValidPdf_shouldSaveSuccessfully() { }
void uploadDocument_withInvalidFile_shouldThrowException() { }
```

## Architecture Guidelines

### Service Layer

- Keep business logic in service classes
- Use DTOs for data transfer between layers
- Don't expose entities directly in controllers
- Handle transactions at the service layer

### Repository Layer

- Use Spring Data JPA repositories
- Create custom queries only when necessary
- Use method naming conventions for simple queries
- Optimize queries for performance

### Controller Layer

- Keep controllers thin
- Validate input using `@Valid`
- Return appropriate HTTP status codes
- Handle exceptions with `@ExceptionHandler`

## Questions or Need Help?

- Open an issue for bugs or feature requests
- Join our discussions for questions
- Review existing issues before creating new ones

## Recognition

Contributors will be recognized in:
- README.md Contributors section
- Release notes
- Project documentation

Thank you for contributing to Document AI Hub! 🎉
