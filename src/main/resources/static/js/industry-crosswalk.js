/**
 * Industry Crosswalk Module
 * Handles cross-referencing between NAICS, SIC and ISIC codes
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const naicsCodeInput = document.getElementById('naicsCode');
    const sicCodeInput = document.getElementById('sicCode');
    const isicCodeInput = document.getElementById('isicCode');
    const sicDescriptionElement = document.getElementById('sicDescription');
    const isicDescriptionElement = document.getElementById('isicDescription');
    const crosswalkSuggestionsElement = document.getElementById('crosswalkSuggestions');
    const suggestedCodesElement = document.getElementById('suggestedCodes');
    const acceptSuggestionsButton = document.getElementById('acceptSuggestions');
    const editSuggestionsButton = document.getElementById('editSuggestions');
    const lookupSicBtn = document.getElementById('lookupSicBtn');
    const lookupIsicBtn = document.getElementById('lookupIsicBtn');
    
    // SIC Badge elements
    const selectedSicBadge = document.getElementById('selectedSicBadge');
    const selectedSicCode = document.getElementById('selectedSicCode');
    const selectedSicTitle = document.getElementById('selectedSicTitle');
    const clearSicSelection = document.getElementById('clearSicSelection');
    
    // ISIC Badge elements
    const selectedIsicBadge = document.getElementById('selectedIsicBadge');
    const selectedIsicCode = document.getElementById('selectedIsicCode');
    const selectedIsicTitle = document.getElementById('selectedIsicTitle');
    const clearIsicSelection = document.getElementById('clearIsicSelection');
    
    // Current suggestions state
    let currentSuggestions = {
        naicsCode: null,
        naicsTitle: null,
        sicCode: null,
        sicTitle: null,
        isicCode: null,
        isicTitle: null
    };
    
    // Initialize
    initCrosswalk();
    
    /**
     * Initialize the crosswalk functionality
     */
    function initCrosswalk() {
        // Listen for changes to NAICS code
        if (naicsCodeInput) {
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'attributes' && mutation.attributeName === 'value') {
                        const naicsCode = naicsCodeInput.value;
                        if (naicsCode && naicsCode.length >= 4) {
                            fetchCrosswalkSuggestions(naicsCode);
                        } else {
                            hideCrosswalkSuggestions();
                        }
                    }
                });
            });
            
            observer.observe(naicsCodeInput, { attributes: true });
            
            // Also check for value changes via events
            document.addEventListener('naicsCodeSelected', function(e) {
                const naicsCode = e.detail.code;
                if (naicsCode && naicsCode.length >= 4) {
                    fetchCrosswalkSuggestions(naicsCode);
                } else {
                    hideCrosswalkSuggestions();
                }
            });
        }
        
        // SIC code lookup button
        if (lookupSicBtn) {
            lookupSicBtn.addEventListener('click', function() {
                const sicCode = sicCodeInput.value.trim();
                if (sicCode) {
                    lookupSicCode(sicCode);
                }
            });
        }
        
        // ISIC code lookup button
        if (lookupIsicBtn) {
            lookupIsicBtn.addEventListener('click', function() {
                const isicCode = isicCodeInput.value.trim();
                if (isicCode) {
                    lookupIsicCode(isicCode);
                }
            });
        }
        
        // SIC code input validation and auto-lookup
        if (sicCodeInput) {
            sicCodeInput.addEventListener('input', function() {
                // Force numeric input for SIC code
                this.value = this.value.replace(/[^0-9]/g, '').substr(0, 4);
                
                // Hide the badge when input changes
                hideSicBadge();
                
                // Auto-lookup if we have 4 digits
                if (this.value.length === 4) {
                    lookupSicCode(this.value);
                } else {
                    sicDescriptionElement.textContent = '';
                }
            });
        }
        
        // ISIC code input validation and auto-lookup
        if (isicCodeInput) {
            isicCodeInput.addEventListener('input', function() {
                // Force numeric input for ISIC code
                this.value = this.value.replace(/[^0-9]/g, '').substr(0, 4);
                
                // Hide the badge when input changes
                hideIsicBadge();
                
                // Auto-lookup if we have 4 digits
                if (this.value.length === 4) {
                    lookupIsicCode(this.value);
                } else {
                    isicDescriptionElement.textContent = '';
                }
            });
        }
        
        // Clear SIC badge button
        if (clearSicSelection) {
            clearSicSelection.addEventListener('click', function(e) {
                e.preventDefault();
                clearSicCode();
            });
        }
        
        // Clear ISIC badge button
        if (clearIsicSelection) {
            clearIsicSelection.addEventListener('click', function(e) {
                e.preventDefault();
                clearIsicCode();
            });
        }
        
        // Accept suggestions button
        if (acceptSuggestionsButton) {
            acceptSuggestionsButton.addEventListener('click', function() {
                applySuggestions();
            });
        }
        
        // Edit suggestions button
        if (editSuggestionsButton) {
            editSuggestionsButton.addEventListener('click', function() {
                // Just expand the accordion to show the edit fields
                const additionalCodesCollapse = document.getElementById('additionalCodesCollapse');
                const bsCollapse = new bootstrap.Collapse(additionalCodesCollapse, {
                    show: true
                });
            });
        }
    }
    
    /**
     * Clear SIC code selection
     */
    function clearSicCode() {
        sicCodeInput.value = '';
        sicDescriptionElement.textContent = '';
        hideSicBadge();
    }
    
    /**
     * Clear ISIC code selection
     */
    function clearIsicCode() {
        isicCodeInput.value = '';
        isicDescriptionElement.textContent = '';
        hideIsicBadge();
    }
    
    /**
     * Show SIC badge with code and title
     */
    function showSicBadge(code, title) {
        if (selectedSicCode && selectedSicTitle && selectedSicBadge) {
            selectedSicCode.textContent = code;
            selectedSicTitle.textContent = title;
            selectedSicBadge.classList.remove('d-none');
        }
    }
    
    /**
     * Hide SIC badge
     */
    function hideSicBadge() {
        if (selectedSicBadge) {
            selectedSicBadge.classList.add('d-none');
        }
    }
    
    /**
     * Show ISIC badge with code and title
     */
    function showIsicBadge(code, title) {
        if (selectedIsicCode && selectedIsicTitle && selectedIsicBadge) {
            selectedIsicCode.textContent = code;
            selectedIsicTitle.textContent = title;
            selectedIsicBadge.classList.remove('d-none');
        }
    }
    
    /**
     * Hide ISIC badge
     */
    function hideIsicBadge() {
        if (selectedIsicBadge) {
            selectedIsicBadge.classList.add('d-none');
        }
    }
    
    /**
     * Fetch crosswalk suggestions based on NAICS code
     */
    function fetchCrosswalkSuggestions(naicsCode) {
        fetch(`/api/v1/crosswalk/naics/${naicsCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Store the suggestions
                currentSuggestions = data;
                
                // Only show suggestions if we have SIC or ISIC mappings
                if (data.sicCode || data.isicCode) {
                    showCrosswalkSuggestions(data);
                } else {
                    hideCrosswalkSuggestions();
                }
            })
            .catch(error => {
                console.error('Error fetching crosswalk suggestions:', error);
                hideCrosswalkSuggestions();
            });
    }
    
    /**
     * Show crosswalk suggestions in the UI
     */
    function showCrosswalkSuggestions(suggestions) {
        let suggestionHtml = '';
        
        if (suggestions.sicCode) {
            suggestionHtml += `<div><strong>SIC:</strong> ${suggestions.sicCode} - ${suggestions.sicTitle || ''}</div>`;
        }
        
        if (suggestions.isicCode) {
            suggestionHtml += `<div><strong>ISIC:</strong> ${suggestions.isicCode} - ${suggestions.isicTitle || ''}</div>`;
        }
        
        suggestedCodesElement.innerHTML = suggestionHtml;
        crosswalkSuggestionsElement.classList.remove('d-none');
    }
    
    /**
     * Hide the crosswalk suggestions
     */
    function hideCrosswalkSuggestions() {
        crosswalkSuggestionsElement.classList.add('d-none');
        suggestedCodesElement.innerHTML = '';
    }
    
    /**
     * Apply the suggested SIC and ISIC codes to the form
     */
    function applySuggestions() {
        if (currentSuggestions.sicCode && sicCodeInput) {
            sicCodeInput.value = currentSuggestions.sicCode;
            sicDescriptionElement.textContent = currentSuggestions.sicTitle || '';
            showSicBadge(currentSuggestions.sicCode, currentSuggestions.sicTitle || '');
        }
        
        if (currentSuggestions.isicCode && isicCodeInput) {
            isicCodeInput.value = currentSuggestions.isicCode;
            isicDescriptionElement.textContent = currentSuggestions.isicTitle || '';
            showIsicBadge(currentSuggestions.isicCode, currentSuggestions.isicTitle || '');
        }
        
        // Hide the suggestions after applying
        hideCrosswalkSuggestions();
    }
    
    /**
     * Look up a SIC code
     */
    function lookupSicCode(sicCode) {
        fetch(`/api/v1/sic/${sicCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Update the description text and badge
                sicDescriptionElement.textContent = data.title || '';
                showSicBadge(sicCode, data.title || '');
                
                // If we don't have a NAICS code selected, suggest one based on SIC
                if (!naicsCodeInput.value) {
                    fetchCrosswalkFromSic(sicCode);
                }
            })
            .catch(error => {
                console.error('Error looking up SIC code:', error);
                sicDescriptionElement.textContent = 'Code not found';
                hideSicBadge();
            });
    }
    
    /**
     * Look up an ISIC code
     */
    function lookupIsicCode(isicCode) {
        fetch(`/api/v1/isic/${isicCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Update the description text and badge
                isicDescriptionElement.textContent = data.title || '';
                showIsicBadge(isicCode, data.title || '');
                
                // If we don't have a NAICS code selected, suggest one based on ISIC
                if (!naicsCodeInput.value) {
                    fetchCrosswalkFromIsic(isicCode);
                }
            })
            .catch(error => {
                console.error('Error looking up ISIC code:', error);
                isicDescriptionElement.textContent = 'Code not found';
                hideIsicBadge();
            });
    }
    
    /**
     * Fetch NAICS suggestion based on SIC code
     */
    function fetchCrosswalkFromSic(sicCode) {
        fetch(`/api/v1/crosswalk/sic/${sicCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // If we got a NAICS mapping, emit an event to select it
                if (data.naicsCode) {
                    document.dispatchEvent(new CustomEvent('suggestNaicsCode', {
                        detail: { code: data.naicsCode }
                    }));
                }
            })
            .catch(error => {
                console.error('Error fetching crosswalk from SIC:', error);
            });
    }
    
    /**
     * Fetch NAICS suggestion based on ISIC code
     */
    function fetchCrosswalkFromIsic(isicCode) {
        fetch(`/api/v1/crosswalk/isic/${isicCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // If we got a NAICS mapping, emit an event to select it
                if (data.naicsCode) {
                    document.dispatchEvent(new CustomEvent('suggestNaicsCode', {
                        detail: { code: data.naicsCode }
                    }));
                }
            })
            .catch(error => {
                console.error('Error fetching crosswalk from ISIC:', error);
            });
    }
}); 