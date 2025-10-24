document.addEventListener('DOMContentLoaded', function() {
    // Get elements
    const userEmailElement = document.getElementById('userEmail');
    const logoutBtn = document.getElementById('logoutBtn');
    
    // Load and display user information
    loadUserInfo();
    
    // Logout button click handler
    logoutBtn.addEventListener('click', function(event) {
        event.preventDefault();
        logout();
    });
    
    // Load user information from storage and display
    function loadUserInfo() {
        // Get user email from local storage
        const userEmail = localStorage.getItem('userEmail') || sessionStorage.getItem('userEmail');
        
        if (userEmail) {
            userEmailElement.textContent = userEmail;
        } else {
            // Redirect to login if no user info found
            window.location.href = '/login';
        }
    }
    
    // Logout function
    function logout() {
        // Show confirmation
        if (confirm('Are you sure you want to log out?')) {
            // Clear auth data
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('authToken');
            localStorage.removeItem('userGroup');
            
            // Keep email for convenience on login page
            // localStorage.removeItem('userEmail');
            
            // Redirect to login page
            window.location.href = '/login';
        }
    }
}); 