document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Add the "Start Onboarding" button click handler
    const startButtons = document.querySelectorAll('.action-btn');
    startButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            redirectToOnboarding();
        });
    });

    // Add Save as Draft button click handler
    const saveButton = document.querySelector('.save-btn');
    if (saveButton) {
        saveButton.addEventListener('click', function() {
            // Show saving indicator
            const originalText = saveButton.textContent;
            saveButton.disabled = true;
            saveButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
            
            // Simulate saving (in a real app, this would save to the server)
            setTimeout(() => {
                saveButton.innerHTML = '<i class="bi bi-check-circle"></i> Saved!';
                
                // Restore button after a delay
                setTimeout(() => {
                    saveButton.disabled = false;
                    saveButton.textContent = originalText;
                    
                    // Show success message
                    showMessage('Draft saved successfully. You can return to continue anytime.', 'success');
                }, 1000);
            }, 1500);
        });
    }

    // Handle resource card clicks
    const infoCards = document.querySelectorAll('.info-card');
    infoCards.forEach(card => {
        card.addEventListener('click', function() {
            const link = this.querySelector('a');
            if (link) {
                link.click();
            }
        });
    });

    // Function to redirect to onboarding
    function redirectToOnboarding() {
        // Get user info from localStorage or set default values
        const userEmail = localStorage.getItem('userEmail') || '';
        
        // Save flag indicating this is after first login
        localStorage.setItem('firstLogin', 'completed');
        
        // Redirect to onboarding page
        window.location.href = '/onboarding';
    }
    
    // Function to show messages
    function showMessage(message, type = 'info') {
        // Remove any existing alerts
        const existingAlerts = document.querySelectorAll('.alert-floating');
        existingAlerts.forEach(alert => alert.remove());
        
        // Create alert element
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible alert-floating fade show`;
        alertDiv.style.position = 'fixed';
        alertDiv.style.top = '20px';
        alertDiv.style.right = '20px';
        alertDiv.style.zIndex = '9999';
        alertDiv.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
        alertDiv.style.minWidth = '300px';
        
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        // Add to body
        document.body.appendChild(alertDiv);
        
        // Auto dismiss after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }

    // Check if user is logged in, if not redirect to login
    function checkLoginStatus() {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        if (!token) {
            // User is not logged in, redirect to login page
            window.location.href = '/login';
        }
    }

    // Call the function to check login status
    checkLoginStatus();
}); 