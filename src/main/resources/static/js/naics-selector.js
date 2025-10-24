/**
 * NAICS Code Selector
 * - Handles the cascading dropdown logic for NAICS code selection
 * - Loads data from the backend API
 * - Stores the selected NAICS code in the form
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const naicsSelectors = document.querySelectorAll('.naics-level select');
    const naicsCodeInput = document.getElementById('naicsCode');
    const selectedNaicsBadge = document.getElementById('selectedNaicsBadge');
    const selectedNaicsCode = document.getElementById('selectedNaicsCode');
    const selectedNaicsTitle = document.getElementById('selectedNaicsTitle');
    const clearNaicsSelection = document.getElementById('clearNaicsSelection');
    
    // New direct search elements
    const naicsSearchInput = document.getElementById('naicsSearchInput');
    const naicsSearchBtn = document.getElementById('naicsSearchBtn');
    const naicsSearchResults = document.getElementById('naicsSearchResults');
    const naicsSearchError = document.getElementById('naicsSearchError');

    // Current selection state
    let selectedNaics = {
        level1: { code: '', title: '' },
        level2: { code: '', title: '' },
        level3: { code: '', title: '' },
        level4: { code: '', title: '' },
        level5: { code: '', title: '' }
    };
    
    // Current level we're at
    let currentMaxLevel = 1;
    
    // Initialize
    initNaicsSelector();
    
    /**
     * Initialize the NAICS code selector
     */
    function initNaicsSelector() {
        // Load top level NAICS codes
        loadNaicsCodes(1);
        
        // Add event listeners to selectors
        naicsSelectors.forEach(selector => {
            // Add search capability to dropdowns
            $(selector).select2({
                placeholder: selector.options[0].textContent,
                allowClear: true,
                width: '100%',
                templateResult: formatNaicsOption
            });
            
            // Handle change events
            $(selector).on('change', function() {
                const level = parseInt(this.getAttribute('data-level'));
                const selectedCode = this.value;
                let selectedTitle = '';
                
                if (selectedCode) {
                    const selectedOption = this.options[this.selectedIndex];
                    selectedTitle = selectedOption.textContent;
                }
                
                // Update selection state
                updateSelectionState(level, selectedCode, selectedTitle);
                
                // Hide any child levels
                hideChildLevels(level);
                
                // If a valid code is selected, load the next level
                if (selectedCode) {
                    loadChildCodes(selectedCode, level + 1);
                }
                
                // Update the display and hidden input
                updateNaicsDisplay();
                
                // Emit an event for crosswalk to pick up
                if (selectedCode) {
                    document.dispatchEvent(new CustomEvent('naicsCodeSelected', {
                        detail: { code: selectedCode }
                    }));
                }
            });
        });
        
        // Clear button click handler
        if (clearNaicsSelection) {
            clearNaicsSelection.addEventListener('click', function(e) {
                e.preventDefault();
                resetNaicsSelection();
                
                // Clear any crosswalk suggestions
                document.dispatchEvent(new CustomEvent('naicsCodeSelected', {
                    detail: { code: '' }
                }));
            });
        }
        
        // Initialize direct search functionality
        if (naicsSearchInput && naicsSearchBtn) {
            // Search button click handler
            naicsSearchBtn.addEventListener('click', function() {
                const searchTerm = naicsSearchInput.value.trim();
                if (searchTerm) {
                    searchNaics(searchTerm);
                } else {
                    hideSearchResults();
                }
            });
            
            // Search input enter key handler
            naicsSearchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    const searchTerm = this.value.trim();
                    if (searchTerm) {
                        searchNaics(searchTerm);
                    } else {
                        hideSearchResults();
                    }
                }
            });
            
            // Clear search results when input changes
            naicsSearchInput.addEventListener('input', function() {
                hideSearchResults();
                
                // Auto-search after typing stops
                clearTimeout(this.searchTimeout);
                this.searchTimeout = setTimeout(() => {
                    const searchTerm = this.value.trim();
                    if (searchTerm && searchTerm.length >= 3) {
                        searchNaics(searchTerm);
                    }
                }, 500);
            });
        }
        
        // Listen for suggestions from crosswalk
        document.addEventListener('suggestNaicsCode', function(e) {
            const naicsCode = e.detail.code;
            if (naicsCode) {
                loadSavedNaicsCode(naicsCode);
            }
        });
        
        // Check if we have a saved value to restore
        const savedCode = naicsCodeInput.value;
        if (savedCode) {
            loadSavedNaicsCode(savedCode);
        }
    }
    
    /**
     * Search for NAICS codes by code or title
     */
    function searchNaics(query) {
        // Show loading state
        naicsSearchInput.classList.add('loading');
        hideSearchResults();
        
        // Call the search API
        fetch(`/api/v1/naics/search?query=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(results => {
                naicsSearchInput.classList.remove('loading');
                
                if (results && results.length > 0) {
                    displaySearchResults(results);
                } else {
                    showSearchError();
                }
            })
            .catch(error => {
                console.error('Error searching NAICS codes:', error);
                naicsSearchInput.classList.remove('loading');
                showSearchError();
            });
    }
    
    /**
     * Display NAICS search results
     */
    function displaySearchResults(results) {
        // Clear existing results
        naicsSearchResults.innerHTML = '';
        
        // Create a list of results
        const resultsList = document.createElement('ul');
        resultsList.className = 'list-group';
        
        // Add each result as a list item
        results.forEach(result => {
            const listItem = document.createElement('li');
            listItem.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-start';
            
            const codeSpan = document.createElement('span');
            codeSpan.className = 'fw-bold';
            codeSpan.textContent = result.code;
            
            const titleSpan = document.createElement('span');
            titleSpan.className = 'ms-2 me-auto';
            titleSpan.textContent = result.title;
            
            listItem.appendChild(codeSpan);
            listItem.appendChild(titleSpan);
            
            // Add click handler to select this code
            listItem.addEventListener('click', function() {
                // Reset the cascading selection
                resetNaicsSelection();
                
                // Set the search input to the selected code
                naicsSearchInput.value = result.code;
                
                // Load the selected code
                loadSavedNaicsCode(result.code);
                
                // Hide search results
                hideSearchResults();
            });
            
            resultsList.appendChild(listItem);
        });
        
        // Add to the search results container
        naicsSearchResults.appendChild(resultsList);
        
        // Show the search results
        naicsSearchResults.classList.remove('d-none');
        naicsSearchError.classList.add('d-none');
    }
    
    /**
     * Show search error message
     */
    function showSearchError() {
        naicsSearchResults.classList.add('d-none');
        naicsSearchError.classList.remove('d-none');
        naicsSearchError.textContent = `No results found for "${naicsSearchInput.value}". Try a different search term or browse using the dropdown below.`;
    }
    
    /**
     * Hide search results and errors
     */
    function hideSearchResults() {
        naicsSearchResults.classList.add('d-none');
        naicsSearchError.classList.add('d-none');
    }
    
    /**
     * Format NAICS option for better display in Select2
     */
    function formatNaicsOption(option) {
        if (!option.id) {
            return option.text;
        }
        
        const text = option.text;
        const parts = text.split(' - ');
        
        if (parts.length < 2) {
            return text;
        }
        
        const code = parts[0].trim();
        const title = parts[1].trim();
        
        // Determine the sector from the first 2 digits
        let sectorInfo = '';
        if (code.length >= 2) {
            sectorInfo = getSectorInfo(code.substring(0, 2));
        }
        
        const $option = $(
            `<div class="naics-option">
                <div class="d-flex justify-content-between">
                    <span class="code">${code}</span>
                    ${sectorInfo ? `<span class="sector text-muted">${sectorInfo}</span>` : ''}
                </div>
                <div class="title">${title}</div>
            </div>`
        );
        
        return $option;
    }
    
    /**
     * Get the sector name for a 2-digit NAICS code
     */
    function getSectorInfo(twoDigitCode) {
        const sectors = {
            '11': 'Agriculture, Forestry, Fishing & Hunting',
            '21': 'Mining, Quarrying, Oil & Gas Extraction',
            '22': 'Utilities',
            '23': 'Construction',
            '31': 'Manufacturing',
            '32': 'Manufacturing',
            '33': 'Manufacturing',
            '42': 'Wholesale Trade',
            '44': 'Retail Trade',
            '45': 'Retail Trade',
            '48': 'Transportation & Warehousing',
            '49': 'Transportation & Warehousing',
            '51': 'Information',
            '52': 'Finance & Insurance',
            '53': 'Real Estate & Rental & Leasing',
            '54': 'Professional, Scientific & Technical Services',
            '55': 'Management of Companies & Enterprises',
            '56': 'Administrative & Support & Waste Management',
            '61': 'Educational Services',
            '62': 'Health Care & Social Assistance',
            '71': 'Arts, Entertainment & Recreation',
            '72': 'Accommodation & Food Services',
            '81': 'Other Services (except Public Administration)',
            '92': 'Public Administration'
        };
        
        return sectors[twoDigitCode] || '';
    }
    
    /**
     * Load NAICS codes for a specific level
     */
    function loadNaicsCodes(level) {
        // Get the selector for this level
        const selector = document.getElementById(`naicsLevel${level}`);
        if (!selector) return;
        
        // Show the container for this level
        const container = document.getElementById(`naicsLevel${level}Container`);
        if (container) {
            container.classList.remove('d-none');
            currentMaxLevel = Math.max(currentMaxLevel, level);
        }
        
        // API endpoint based on level
        let url = '/api/v1/naics';
        
        if (level > 1) {
            // For higher levels, we need the parent code
            const parentLevelSelector = document.getElementById(`naicsLevel${level-1}`);
            if (!parentLevelSelector || !parentLevelSelector.value) return;
            
            url = `/api/v1/naics/children/${parentLevelSelector.value}`;
        }
        
        // Fetch data from API
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Clear current options except the default
                while (selector.options.length > 1) {
                    selector.remove(1);
                }
                
                // Add new options
                data.forEach(naicsCode => {
                    const option = document.createElement('option');
                    option.value = naicsCode.code;
                    option.textContent = `${naicsCode.code} - ${naicsCode.title}`;
                    selector.appendChild(option);
                });
                
                // If no options were added (empty response), hide this level
                if (data.length === 0) {
                    container.classList.add('d-none');
                }
                
                // Refresh the select2 dropdown
                $(selector).trigger('change.select2');
            })
            .catch(error => {
                console.error('Error loading NAICS codes:', error);
            });
    }
    
    /**
     * Load child NAICS codes for a parent code
     */
    function loadChildCodes(parentCode, childLevel) {
        // If we've reached the maximum depth, stop
        if (childLevel > 5) return;
        
        // Get the selector for the child level
        const childSelector = document.getElementById(`naicsLevel${childLevel}`);
        if (!childSelector) return;
        
        // Show the container for the child level
        const container = document.getElementById(`naicsLevel${childLevel}Container`);
        if (container) {
            container.classList.remove('d-none');
            currentMaxLevel = Math.max(currentMaxLevel, childLevel);
        }
        
        // Fetch child codes from API
        fetch(`/api/v1/naics/children/${parentCode}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Clear current options except the default
                while (childSelector.options.length > 1) {
                    childSelector.remove(1);
                }
                
                // Add new options
                data.forEach(naicsCode => {
                    const option = document.createElement('option');
                    option.value = naicsCode.code;
                    option.textContent = `${naicsCode.code} - ${naicsCode.title}`;
                    childSelector.appendChild(option);
                });
                
                // If no options were added (empty response), hide this level
                if (data.length === 0) {
                    container.classList.add('d-none');
                }
                
                // Refresh the select2 dropdown
                $(childSelector).trigger('change.select2');
            })
            .catch(error => {
                console.error('Error loading child NAICS codes:', error);
            });
    }
    
    /**
     * Update the selection state based on user selection
     */
    function updateSelectionState(level, code, title) {
        // Update the state for this level
        selectedNaics[`level${level}`] = { code, title };
        
        // Clear any lower levels
        for (let i = level + 1; i <= 5; i++) {
            selectedNaics[`level${i}`] = { code: '', title: '' };
        }
    }
    
    /**
     * Hide all child levels after the specified level
     */
    function hideChildLevels(parentLevel) {
        for (let i = parentLevel + 1; i <= 5; i++) {
            const container = document.getElementById(`naicsLevel${i}Container`);
            if (container) {
                container.classList.add('d-none');
            }
            
            // Reset the selector value
            const selector = document.getElementById(`naicsLevel${i}`);
            if (selector) {
                selector.value = '';
                // Refresh the select2 dropdown
                $(selector).trigger('change.select2');
            }
        }
    }
    
    /**
     * Update the display and hidden input with current selection
     */
    function updateNaicsDisplay() {
        // Find the deepest selected level
        let deepestLevel = 0;
        let deepestCode = '';
        let deepestTitle = '';
        
        for (let i = 5; i >= 1; i--) {
            if (selectedNaics[`level${i}`].code) {
                deepestLevel = i;
                deepestCode = selectedNaics[`level${i}`].code;
                deepestTitle = selectedNaics[`level${i}`].title;
                break;
            }
        }
        
        // Update the hidden input with the selected code
        if (deepestCode) {
            naicsCodeInput.value = deepestCode;
            selectedNaicsCode.textContent = deepestCode;
            selectedNaicsTitle.textContent = ` - ${deepestTitle.split(' - ')[1] || deepestTitle}`;
            selectedNaicsBadge.classList.remove('d-none');
        } else {
            naicsCodeInput.value = '';
            selectedNaicsBadge.classList.add('d-none');
        }
    }
    
    /**
     * Reset the NAICS selection
     */
    function resetNaicsSelection() {
        // Reset the state
        for (let i = 1; i <= 5; i++) {
            selectedNaics[`level${i}`] = { code: '', title: '' };
            
            // Reset the selector value
            const selector = document.getElementById(`naicsLevel${i}`);
            if (selector) {
                selector.value = '';
                // Refresh the select2 dropdown
                $(selector).trigger('change.select2');
            }
            
            // Hide child level containers (except level 1)
            if (i > 1) {
                const container = document.getElementById(`naicsLevel${i}Container`);
                if (container) {
                    container.classList.add('d-none');
                }
            }
        }
        
        // Update the display
        naicsCodeInput.value = '';
        selectedNaicsBadge.classList.add('d-none');
        
        // Reset to show only level 1
        currentMaxLevel = 1;
    }
    
    /**
     * Load and select a saved NAICS code
     */
    function loadSavedNaicsCode(naicsCode) {
        // Fetch the hierarchy for this code
        fetch(`/api/v1/naics/${naicsCode}/hierarchy`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(hierarchy => {
                // Reset current selection first
                resetNaicsSelection();
                
                // Process each level in the hierarchy
                for (let i = 0; i < hierarchy.length; i++) {
                    const level = i + 1;
                    const code = hierarchy[i].code;
                    const title = hierarchy[i].title;
                    
                    // First load the options for this level
                    if (i === 0) {
                        // For level 1, just load all top level codes
                        loadNaicsCodes(1);
                    } else {
                        // For other levels, load children of the parent
                        const parentCode = hierarchy[i-1].code;
                        loadChildCodes(parentCode, level);
                    }
                    
                    // Then set the selection for this level
                    setTimeout(() => {
                        const selector = document.getElementById(`naicsLevel${level}`);
                        if (selector) {
                            // Set the selector value
                            selector.value = code;
                            
                            // Refresh the select2 dropdown
                            $(selector).val(code).trigger('change');
                            
                            // Update our state
                            updateSelectionState(level, code, `${code} - ${title}`);
                            
                            // Update display
                            updateNaicsDisplay();
                            
                            // Emit an event for crosswalk if we've reached the final level
                            if (i === hierarchy.length - 1) {
                                document.dispatchEvent(new CustomEvent('naicsCodeSelected', {
                                    detail: { code: code }
                                }));
                            }
                        }
                    }, 500 * i); // Add delay to ensure previous level is loaded
                }
            })
            .catch(error => {
                console.error('Error loading NAICS hierarchy:', error);
            });
    }
}); 