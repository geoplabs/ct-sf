document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const companyNameInput = document.getElementById('companyName');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Form submission handler
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        // Reset validation state
        resetValidation();
        
        // Validate the form
        if (validateForm()) {
            // If validation passes, submit the form
            submitForm();
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
        
        // First name validation
        if (firstNameInput.value.trim() === '') {
            setInvalid(firstNameInput, 'First name is required.');
            isValid = false;
        } else {
            setValid(firstNameInput);
        }
        
        // Last name validation
        if (lastNameInput.value.trim() === '') {
            setInvalid(lastNameInput, 'Last name is required.');
            isValid = false;
        } else {
            setValid(lastNameInput);
        }
        
        // Company name validation
        if (companyNameInput.value.trim() === '') {
            setInvalid(companyNameInput, 'Company name is required.');
            isValid = false;
        } else {
            setValid(companyNameInput);
        }
        
        // Password validation
        if (passwordInput.value.length < 8) {
            setInvalid(passwordInput, 'Password must be at least 8 characters.');
            isValid = false;
        } else {
            setValid(passwordInput);
        }
        
        // Confirm password validation
        if (confirmPasswordInput.value !== passwordInput.value) {
            setInvalid(confirmPasswordInput, 'Passwords do not match.');
            isValid = false;
        } else {
            setValid(confirmPasswordInput);
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

    // Submit the form data to the API
    async function submitForm() {
        // Show loading indicator
        const submitButton = form.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.textContent;
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating account...';
        
        try {
            // First create the company group
            const groupResponse = await createCompanyGroup();
            
            if (groupResponse.ok) {
                // Update loading indicator for second step
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Setting up user account...';
                
                // If group creation was successful, create the user in that group
                const companyName = companyNameInput.value.trim();
                
                // Format the ID in the same way as in createCompanyGroup
                const formattedId = companyName
                    .toLowerCase()
                    .replace(/\s+/g, '-')
                    .replace(/[^a-z0-9-]/g, '')
                    .replace(/-{2,}/g, '-');
                
                const companyId = formattedId ? `/${formattedId}` : '/unknown-company';
                
                const userResponse = await createUser(companyId);
                
                if (userResponse.ok) {
                    // Both operations successful, redirect to success page or dashboard
                    window.location.href = '/signup-success';
                } else {
                    // Handle user creation error
                    console.error('User creation failed with status:', userResponse.status);
                    try {
                        const errorData = await userResponse.json();
                        console.error('Error data:', errorData);
                        
                        if (errorData && errorData.message) {
                            showError(errorData.message);
                        } else {
                            showError('Failed to create account. Please try again later.');
                        }
                    } catch (e) {
                        // If we can't parse the JSON, just show the raw text
                        const responseText = await userResponse.text();
                        console.error('Response:', responseText);
                        showError('Failed to create account. Please try again later.');
                    }
                }
            } else {
                // Handle group creation error
                console.error('Group creation failed with status:', groupResponse.status);
                try {
                    const errorData = await groupResponse.json();
                    console.error('Error data:', errorData);
                    
                    if (errorData && errorData.message) {
                        showError(errorData.message);
                    } else {
                        showError('Failed to create company. Please try again later.');
                    }
                } catch (e) {
                    // If we can't parse the JSON, just show the raw text
                    const responseText = await groupResponse.text();
                    console.error('Response:', responseText);
                    showError('Failed to create company. Please try again later.');
                }
            }
        } catch (error) {
            console.error('Error during sign up:', error);
            showError('Network error. Please check your connection and try again.');
        } finally {
            // Restore button state
            submitButton.disabled = false;
            submitButton.textContent = originalButtonText;
        }
    }

    // Create a new user via API
    async function createUser(groupId) {
        // Ensure first and last names are strings, not null
        const firstName = firstNameInput.value.trim();
        const lastName = lastNameInput.value.trim();
        const companyName = companyNameInput.value.trim();
        
        // Create user data with explicit fields
        const userData = {
            email: emailInput.value.trim(),
            firstName: firstName,  // Explicitly set as string
            lastName: lastName,    // Explicitly set as string
            role: 'ADMIN',         // New users are assigned admin role for their company
            password: passwordInput.value,
            tags: {
                company: companyName, // Use the original company name with all special characters
                title: ""
            },
            // Add defaultGroup explicitly
            defaultGroup: groupId
        };

        console.log('Creating user with data:', {
            ...userData,
            password: '********' // Hide actual password in logs
        });

        // Ensure we're sending a properly formatted JSON payload
        const requestBody = JSON.stringify(userData);
        console.log('Request body:', requestBody);
        
        // Use explicit request options to ensure headers are correctly set
        const requestOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Group-Context': groupId,
                'Accept': 'application/json'
            },
            body: requestBody
        };
        
        // Make the API call with the request options
        return fetch('/api/v1/users', requestOptions);
    }

    // Create a new company group via API
    async function createCompanyGroup() {
        const companyName = companyNameInput.value.trim();
        
        // Create a proper ID by removing spaces and special characters
        const formattedId = companyName
            .toLowerCase()
            .replace(/\s+/g, '-')         // Replace spaces with hyphens
            .replace(/[^a-z0-9-]/g, '')   // Remove special characters except hyphens
            .replace(/-{2,}/g, '-');      // Replace multiple hyphens with a single one
            
        // Ensure we have a valid ID
        const companyId = formattedId ? `/${formattedId}` : '/unknown-company';
        
        const groupData = {
            id: companyId,           // Explicit company ID with format "/company-name"
            name: companyName,       // Preserve original company name with spaces and special characters
            company: companyName,    // Added company parameter
            description: `Company: ${companyName}`,
            isParentCompany: true,   // Add isParentCompany as a top-level property
            // accountId is not set here - it will be generated by the server
            tags: {
                createdBy: emailInput.value,
                isCompany: "true",
                isParentCompany: "true",  // Add as tag for redundancy
                originalName: companyName // Store the original name with all special characters
            }
        };

        console.log('Creating company group with data:', groupData);
        
        // Use POST to create a new group - ensure path starts with leading slash
        return fetch('/api/v1/groups', {
            method: 'POST',  // Use POST for creation rather than PATCH
            headers: {
                'Content-Type': 'application/json',
                'X-User-Email': emailInput.value,
                'X-Group-Context': '/' // Root group context
            },
            body: JSON.stringify(groupData)
        });
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
}); 