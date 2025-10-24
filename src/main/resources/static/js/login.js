document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const rememberMeCheckbox = document.getElementById('rememberMe');

    // Form submission handler
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        // Reset validation state
        resetValidation();
        
        // Validate the form
        if (validateForm()) {
            // If validation passes, bypass authentication and go directly to onboarding
            bypassLoginForTesting();
             // If validation passes, submit the form
            // loginUser();
        }
    });

    // Validate the form fields
    function validateForm() {
        let isValid = true;
        
        // Email validation
        if (!isValidEmail(emailInput.value)) {
            setInvalid(emailInput, 'Please enter a valid email address.');
            isValid = false;
        } else {
            setValid(emailInput);
        }
        
        // Password validation
        if (passwordInput.value.trim() === '') {
            setInvalid(passwordInput, 'Please enter your password.');
            isValid = false;
        } else {
            setValid(passwordInput);
        }
        
        return isValid;
    }

    // Reset validation state for all fields
    function resetValidation() {
        const inputs = form.querySelectorAll('input');
        inputs.forEach(input => {
            input.classList.remove('is-invalid');
            input.classList.remove('is-valid');
        });
    }

    // Set a field as invalid with a custom message
    function setInvalid(input, message) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
        
        // Update the feedback message if provided
        const feedbackElement = input.nextElementSibling;
        if (feedbackElement && feedbackElement.classList.contains('invalid-feedback')) {
            feedbackElement.textContent = message;
        }
    }

    // Set a field as valid
    function setValid(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
    }

    // Check if email is valid
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // Bypass authentication for testing
    function bypassLoginForTesting() {
        // Store the email for use in the onboarding page
        const email = emailInput.value.trim();
        localStorage.setItem('userEmail', email);
        
        // Set a mock default group ID 
        localStorage.setItem('userGroup', '/');
        
        // Set a mock token for API calls
        localStorage.setItem('authToken', 'mock-token-for-testing');
        
        // Check if this is first login
        const firstLogin = localStorage.getItem('firstLogin');
        
        // Redirect to welcome page for first-time users, onboarding for returning users
        if (firstLogin === 'completed') {
            window.location.href = '/welcome';
        } else {
            window.location.href = '/welcome';
        }
    }

    // Show an error message to the user
    function showError(message) {
        // Check if an error alert already exists and remove it
        const existingAlert = form.querySelector('.alert');
        if (existingAlert) {
            existingAlert.remove();
        }
        
        // Create a new error alert
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger mb-4';
        errorDiv.textContent = message;
        
        // Insert at the top of the form
        form.insertBefore(errorDiv, form.firstChild);
        
        // Scroll to top if needed
        errorDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        
        // Remove after 5 seconds
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    }

    // Try to prefill email if available in local storage
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
        emailInput.value = savedEmail;
    }
}); 