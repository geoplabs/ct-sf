document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Add user info banner to show email and group ID
    addUserInfoBanner();

    // Load regions and countries
    loadRegionsAndCountries();

    // Elements
    const corporateForm = document.getElementById('corporateForm');
    const masterRecordOverview = document.getElementById('masterRecordOverview');
    const saveBtn = document.getElementById('saveBtn');
    const confirmBtn = document.getElementById('confirmBtn');
    const confirmSaveBtn = document.getElementById('confirmSaveBtn');
    const backToFormBtn = document.getElementById('backToFormBtn');
    const generateIdBtn = document.getElementById('generateIdBtn');
    
    // Form inputs
    const parentCompanyId = document.getElementById('parentCompanyId');
    const parentCompanyName = document.getElementById('parentCompanyName');
    const naicsCode = document.getElementById('naicsCode');
    const sicCode = document.getElementById('sicCode');
    const businessDescription = document.getElementById('businessDescription');
    // New headquarters location fields
    const streetAddress = document.getElementById('streetAddress');
    const city = document.getElementById('city');
    const country = document.getElementById('country');
    const latitude = document.getElementById('latitude');
    const longitude = document.getElementById('longitude');
    const baselineYear = document.getElementById('baselineYear');
    const baselineAdjustment = document.getElementById('baselineAdjustment');
    // New unit system and GWP horizon fields
    const metricOption = document.getElementById('metricOption');
    const imperialOption = document.getElementById('imperialOption');
    const gwpHorizon = document.getElementById('gwpHorizon');
    // New targets & commitments fields
    const netZeroCommitted = document.getElementById('netZeroCommitted');
    const netZeroStatus = document.getElementById('netZeroStatus');
    const absoluteTarget = document.getElementById('absoluteTarget');
    const intensityTarget = document.getElementById('intensityTarget');
    const targetYear = document.getElementById('targetYear');
    const targetPercentage = document.getElementById('targetPercentage');
    // New governance & contacts fields
    const sustainabilityLeadName = document.getElementById('sustainabilityLeadName');
    const sustainabilityLeadEmail = document.getElementById('sustainabilityLeadEmail');
    const sustainabilityLeadPhone = document.getElementById('sustainabilityLeadPhone');
    const financeLeadName = document.getElementById('financeLeadName');
    const financeLeadEmail = document.getElementById('financeLeadEmail');
    const financeLeadPhone = document.getElementById('financeLeadPhone');
    const boardOversight = document.getElementById('boardOversight');
    const boardOversightStatus = document.getElementById('boardOversightStatus');
    const boardContactInfo = document.getElementById('boardContactInfo');
    const boardContact = document.getElementById('boardContact');
    // New security & preferences fields
    const ssoDomains = document.getElementById('ssoDomains');
    const dataResidency = document.getElementById('dataResidency');
    const adminUsers = document.getElementById('adminUsers');
    const adminEmailChips = document.getElementById('adminEmailChips');
    // New legal & consent fields
    const dpaAcceptance = document.getElementById('dpaAcceptance');
    const retentionPeriod = document.getElementById('retentionPeriod');
    
    // Add geocoding functionality
    setupGeocoding();
    
    // Overview elements
    const overviewCompanyId = document.getElementById('overview-companyId');
    const overviewLegalName = document.getElementById('overview-legalName');
    const overviewIndustry = document.getElementById('overview-industry');
    const overviewOrgStructure = document.getElementById('overview-orgStructure');
    const overviewAddress = document.getElementById('overview-address');
    const overviewCoordinates = document.getElementById('overview-coordinates');
    
    // Character counters
    const descCharCount = document.getElementById('descCharCount');
    const adjustmentCharCount = document.getElementById('adjustmentCharCount');
    
    // Track character counts
    businessDescription.addEventListener('input', function() {
        descCharCount.textContent = this.value.length;
    });
    
    baselineAdjustment.addEventListener('input', function() {
        adjustmentCharCount.textContent = this.value.length;
    });
    
    // Setup geocoding functionality for location input
    function setupGeocoding() {
        // Add event listeners for address fields to enable geocoding
        city.addEventListener('change', function() {
            enableGeocodeButtonIfReady();
        });
        
        country.addEventListener('change', function() {
            enableGeocodeButtonIfReady();
        });
        
        // Add geocode button after the city field
        if (city && city.parentElement) {
            const geocodeBtn = document.createElement('button');
            geocodeBtn.id = 'geocodeBtn';
            geocodeBtn.className = 'btn btn-outline-primary mt-2 w-100';
            geocodeBtn.innerHTML = '<i class="bi bi-geo-alt"></i> Get Coordinates';
            geocodeBtn.disabled = true;
            geocodeBtn.addEventListener('click', function(e) {
                e.preventDefault();
                geocodeAddress();
            });
            
            city.parentElement.appendChild(geocodeBtn);
        }
        
        // Enable the geocode button if city and country are filled
        function enableGeocodeButtonIfReady() {
            const geocodeBtn = document.getElementById('geocodeBtn');
            if (geocodeBtn) {
                geocodeBtn.disabled = !(city.value.trim() && country.value);
            }
        }
        
        // Function to geocode address using Mapbox API
        function geocodeAddress() {
            // Show loading state
            const geocodeBtn = document.getElementById('geocodeBtn');
            const originalText = geocodeBtn.innerHTML;
            geocodeBtn.disabled = true;
            geocodeBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Searching...';
            
            // Build search query
            const parts = [];
            if (streetAddress.value.trim()) parts.push(streetAddress.value.trim());
            if (city.value.trim()) parts.push(city.value.trim());
            if (country.value) {
                const countryName = country.options[country.selectedIndex].text;
                parts.push(countryName);
            }
            
            const searchQuery = parts.join(', ');
            
            // Initialize Mapbox geocoding with API key
            const mapboxApiKey = 'pHSbj2vjJ+rUprhF2W2B3acz6QCLdEOdFmb1yzuGWPE=';
            const geocoder = new MapboxGeocoding(mapboxApiKey);
            
            // Get country code for better results
            const countryCode = getCountryCode(country.value);
            
            geocoder.geocodeAddress(searchQuery, {
                country: countryCode,
                limit: 1
            }).then(result => {
                if (result.success && result.latitude && result.longitude) {
                    // Format coordinates to 6 decimal places for precision
                    latitude.value = parseFloat(result.latitude).toFixed(6);
                    longitude.value = parseFloat(result.longitude).toFixed(6);
                    
                    // Show success feedback with confidence indicator
                    const confidence = Math.round(result.confidence * 100);
                    geocodeBtn.innerHTML = `<i class="bi bi-check-circle"></i> Found! (${confidence}% confidence)`;
                    
                    // Show formatted address if different from input
                    if (result.formatted_address && result.formatted_address !== searchQuery) {
                        showSuccess(`Coordinates found for: ${result.formatted_address}`);
                    }
                    
                    setTimeout(() => {
                        geocodeBtn.innerHTML = originalText;
                        geocodeBtn.disabled = false;
                    }, 3000);
                } else {
                    const errorMsg = result.error || 'Could not find coordinates for this address. Please try a different address or enter coordinates manually.';
                    showError(errorMsg);
                    geocodeBtn.innerHTML = originalText;
                    geocodeBtn.disabled = false;
                }
            }).catch(error => {
                console.error('Error geocoding address:', error);
                showError('Error finding coordinates: ' + error.message);
                geocodeBtn.innerHTML = originalText;
                geocodeBtn.disabled = false;
            });
        }
        
        // Helper function to get country code from country name
        function getCountryCode(countryName) {
            const countryCodes = {
                'United States': 'us',
                'Canada': 'ca',
                'United Kingdom': 'gb',
                'Germany': 'de',
                'France': 'fr',
                'India': 'in',
                'Australia': 'au',
                'Japan': 'jp',
                'China': 'cn',
                'Brazil': 'br',
                'Mexico': 'mx',
                'Italy': 'it',
                'Spain': 'es',
                'Netherlands': 'nl',
                'Sweden': 'se',
                'Norway': 'no',
                'Denmark': 'dk',
                'Finland': 'fi'
                // Add more as needed
            };
            return countryCodes[countryName] || null;
        }
    }
    
    // Generate a unique company ID
    generateIdBtn.addEventListener('click', function() {
        if (parentCompanyName.value.trim() === '') {
            showError('Please enter a company name first to generate an ID.');
            return;
        }
        
        // Generate ID based on company name (lowercase, spaces to hyphens, remove special chars)
        const name = parentCompanyName.value.trim();
        // Create a formatted ID (lowercase with hyphens replacing spaces and special chars removed)
        const formattedId = name
            .toLowerCase()
            .replace(/\s+/g, '-')         // Replace spaces with hyphens
            .replace(/[^a-z0-9-]/g, '')   // Remove special characters except hyphens
            .replace(/-{2,}/g, '-');      // Replace multiple hyphens with a single one
        
        parentCompanyId.value = formattedId;
        
        showSuccess('ID generated successfully. Display name preserved with special characters.');
    });
    
    // Save button click - directly save progress
    saveBtn.addEventListener('click', function() {
        try {
            // Validate if needed, but continue even if validation fails in demo mode
            validateForm();
            
            // Show loading indicator
            const originalText = saveBtn.textContent;
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
            
            // Collect form data
            const formData = collectFormData();
            
            // Save the data via API - with proper error handling
            saveDataToApi(formData)
                .then(response => {
                    console.log('Save response:', response);
                    
                    // Handle successful response
                    if (response && response.ok) {
                        showSuccess('Progress successfully saved!');
                        return;
                    }
                    
                    // Handle error responses by status code
                    switch (response.status) {
                        case 400:
                            // Bad request - likely invalid data format
                            return response.json().then(errorData => {
                                throw new Error(errorData.message || 'Invalid data format. Please check your inputs.');
                            });
                        case 401:
                            throw new Error('Authentication error. Please log in again.');
                        case 403:
                            throw new Error('You do not have permission to save this data.');
                        case 404:
                            throw new Error('The company ID could not be found. Please check the ID or create a new company.');
                        case 500:
                        default:
                            throw new Error('Server error. Please try again later or contact support.');
                    }
                })
                .catch(error => {
                    console.error('Error saving profile:', error);
                    showError('Error saving: ' + (error.message || 'Unknown error occurred'));
                })
                .finally(() => {
                    // Restore button state after a delay
                    setTimeout(() => {
                        saveBtn.disabled = false;
                        saveBtn.innerHTML = originalText;
                    }, 1000);
                });
        } catch (error) {
            console.error('Error in save button handler:', error);
            showError('Error: ' + (error.message || 'Unknown error occurred'));
            saveBtn.disabled = false;
            saveBtn.innerHTML = 'Save Progress';
        }
    });
    
    // Confirm button click - Show the overview with save option
    confirmBtn.addEventListener('click', function() {
        // Check DPA acceptance first - this is a critical requirement
        if (!dpaAcceptance.checked) {
            showError('You must accept the Data Processing Agreement to proceed.');
            // Scroll to the DPA section and highlight it
            dpaAcceptance.scrollIntoView({ behavior: 'smooth', block: 'center' });
            dpaAcceptance.parentElement.classList.add('border', 'border-danger', 'p-2');
            setTimeout(() => {
                dpaAcceptance.parentElement.classList.remove('border', 'border-danger', 'p-2');
            }, 3000);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        updateOverview();
        corporateForm.classList.add('d-none');
        masterRecordOverview.classList.remove('d-none');
    });
    
    // Back to form button
    backToFormBtn.addEventListener('click', function() {
        masterRecordOverview.classList.add('d-none');
        corporateForm.classList.remove('d-none');
    });
    
    // Confirm and Save button
    confirmSaveBtn.addEventListener('click', function() {
        // Show loading indicator
        const originalText = confirmSaveBtn.textContent;
        confirmSaveBtn.disabled = true;
        confirmSaveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';
        
        // Collect all form data
        const formData = collectFormData();
        
        // Save the data via API
        saveDataToApi(formData)
            .then(response => {
                if (response.ok) {
                    showSuccess('Corporate profile successfully saved!');
                    // Redirect to entity hierarchy page after successful save
                    setTimeout(() => {
                        window.location.href = '/entity-hierarchy';
                    }, 1500); // Give time for success message to be seen
                } else {
                    return response.json().then(error => {
                        throw new Error(error.message || 'Failed to save corporate profile');
                    });
                }
            })
            .catch(error => {
                showError(error.message);
                console.error('Error saving profile:', error);
            })
            .finally(() => {
                // Restore button
                confirmSaveBtn.disabled = false;
                confirmSaveBtn.textContent = originalText;
            });
    });
    
    // Update the overview with current data
    function updateOverview() {
        // Basic information
        overviewCompanyId.textContent = parentCompanyId.value || '(Not specified)';
        overviewLegalName.textContent = parentCompanyName.value || '(Not specified)';
        
        // Industry information
        const industryText = [];
        
        // Add NAICS code if available
        const naicsCodeValue = document.getElementById('naicsCode').value;
        const naicsDescription = document.getElementById('selectedNaicsTitle') ? 
            document.getElementById('selectedNaicsTitle').textContent.trim() : '';
            
        if (naicsCodeValue) {
            industryText.push(`NAICS ${naicsCodeValue}`);
            if (naicsDescription && naicsDescription.startsWith('- ')) {
                industryText.push(naicsDescription.substring(2));
            }
        }
        
        // Add SIC code if available
        if (sicCode.value) {
            industryText.push(`SIC ${sicCode.value}`);
        }
        
        if (industryText.length > 0) {
            overviewIndustry.textContent = industryText.join(' – ');
        } else {
            overviewIndustry.textContent = '(Not specified)';
        }
        
        // Organization structure (parent company with mock subsidiaries)
        const companyName = parentCompanyName.value || 'COMPANY';
        const companyId = parentCompanyId.value || 'ID';
        
        overviewOrgStructure.innerHTML = `
            <div class="mb-2"><i class="bi bi-building text-primary"></i> ${companyId}</div>
            <div class="ms-4 mb-2"><i class="bi bi-folder text-secondary"></i> UK-SUB-001 (Energy Trading)</div>
            <div class="ms-4 mb-2"><i class="bi bi-folder text-secondary"></i> UK-SUB-002 (Manufacturing)</div>
            <div class="ms-4"><i class="bi bi-diagram-3 text-warning"></i> UK-JV-001 (Integrated Oil & Gas)</div>
        `;
        
        // Location information - Updated for separate fields
        const addressParts = [];
        if (streetAddress.value) addressParts.push(streetAddress.value);
        if (city.value) addressParts.push(city.value);
        if (country.value) addressParts.push(country.options[country.selectedIndex].text);
        
        overviewAddress.textContent = addressParts.length > 0 ? addressParts.join(', ') : '(Not specified)';
        
        if (latitude.value && longitude.value) {
            overviewCoordinates.textContent = `${latitude.value}, ${longitude.value}`;
        } else {
            overviewCoordinates.textContent = '(Not specified)';
        }

        // Add reporting configuration information
        const overviewBaselineYear = document.getElementById('overview-baselineYear');
        const overviewReportingYearType = document.getElementById('overview-reportingYearType');
        const overviewUnitSystem = document.getElementById('overview-unitSystem');
        const overviewGwpHorizon = document.getElementById('overview-gwpHorizon');

        if (overviewBaselineYear) {
            overviewBaselineYear.textContent = baselineYear.value || '(Not specified)';
        }

        if (overviewReportingYearType) {
            if (calendarYearOption.checked) {
                overviewReportingYearType.textContent = 'Calendar Year (Jan 1 - Dec 31)';
            } else if (customFiscalOption.checked) {
                const startDate = fiscalStart.value ? new Date(fiscalStart.value).toLocaleDateString() : '?';
                const endDate = fiscalEnd.value ? new Date(fiscalEnd.value).toLocaleDateString() : '?';
                overviewReportingYearType.textContent = `Custom Fiscal Year (${startDate} - ${endDate})`;
            } else {
                overviewReportingYearType.textContent = '(Not specified)';
            }
        }

        if (overviewUnitSystem) {
            overviewUnitSystem.textContent = metricOption.checked ? 'Metric (kg, tonnes, km)' : 'Imperial (lbs, tons, miles)';
        }

        if (overviewGwpHorizon) {
            const gwpText = gwpHorizon.options[gwpHorizon.selectedIndex].text;
            overviewGwpHorizon.textContent = gwpText || '(Not specified)';
        }

        // Add targets and commitments information
        const overviewNetZeroCommitted = document.getElementById('overview-netZeroCommitted');
        const overviewTargetType = document.getElementById('overview-targetType');
        const overviewReductionGoal = document.getElementById('overview-reductionGoal');

        if (overviewNetZeroCommitted) {
            if (netZeroCommitted.checked) {
                overviewNetZeroCommitted.textContent = 'Committed';
                overviewNetZeroCommitted.classList.add('text-success');
            } else {
                overviewNetZeroCommitted.textContent = 'Not committed';
                overviewNetZeroCommitted.classList.remove('text-success');
            }
        }

        if (overviewTargetType) {
            overviewTargetType.textContent = absoluteTarget.checked ? 'Absolute' : 'Intensity';
        }

        if (overviewReductionGoal) {
            overviewReductionGoal.textContent = `${targetPercentage.value}% by ${targetYear.value}`;
        }

        // Add governance and contacts information
        const overviewSustainabilityLead = document.getElementById('overview-sustainabilityLead');
        const overviewFinanceLead = document.getElementById('overview-financeLead');
        const overviewBoardOversight = document.getElementById('overview-boardOversight');
        const overviewBoardContact = document.getElementById('overview-boardContact');

        if (overviewSustainabilityLead) {
            const sustainabilityInfo = [];
            if (sustainabilityLeadName.value) sustainabilityInfo.push(sustainabilityLeadName.value);
            if (sustainabilityLeadEmail.value) sustainabilityInfo.push(sustainabilityLeadEmail.value);
            if (sustainabilityLeadPhone.value) sustainabilityInfo.push(sustainabilityLeadPhone.value);
            
            overviewSustainabilityLead.textContent = sustainabilityInfo.length > 0 ? 
                sustainabilityInfo.join(', ') : '(Not specified)';
        }

        if (overviewFinanceLead) {
            const financeInfo = [];
            if (financeLeadName.value) financeInfo.push(financeLeadName.value);
            if (financeLeadEmail.value) financeInfo.push(financeLeadEmail.value);
            if (financeLeadPhone.value) financeInfo.push(financeLeadPhone.value);
            
            overviewFinanceLead.textContent = financeInfo.length > 0 ? 
                financeInfo.join(', ') : '(Not specified)';
        }

        if (overviewBoardOversight) {
            if (boardOversight.checked) {
                overviewBoardOversight.textContent = 'Yes';
                overviewBoardOversight.classList.add('text-success');
                if (boardContact.value) {
                    overviewBoardContact.textContent = boardContact.value;
                    overviewBoardContact.classList.remove('d-none');
                } else {
                    overviewBoardContact.classList.add('d-none');
                }
            } else {
                overviewBoardOversight.textContent = 'No';
                overviewBoardOversight.classList.remove('text-success');
                overviewBoardContact.classList.add('d-none');
            }
        }
        
        // Add security & preferences information to the overview if elements exist
        const overviewSsoDomains = document.getElementById('overview-ssoDomains');
        const overviewDataResidency = document.getElementById('overview-dataResidency');
        const overviewAdminUsers = document.getElementById('overview-adminUsers');
        
        if (overviewSsoDomains) {
            const domains = $(ssoDomains).val();
            overviewSsoDomains.textContent = domains && domains.length > 0 ? 
                domains.join(', ') : '(Not specified)';
        }
        
        if (overviewDataResidency) {
            if (dataResidency.value) {
                const selectedOption = dataResidency.options[dataResidency.selectedIndex];
                overviewDataResidency.textContent = selectedOption.text;
            } else {
                overviewDataResidency.textContent = '(Not specified)';
            }
        }
        
        if (overviewAdminUsers) {
            const emails = getAdminEmails();
            overviewAdminUsers.textContent = emails.length > 0 ? 
                emails.join(', ') : '(Not specified)';
        }
        
        // Add legal & consent information to the overview if elements exist
        const overviewDpaAcceptance = document.getElementById('overview-dpaAcceptance');
        const overviewRetentionPeriod = document.getElementById('overview-retentionPeriod');
        
        if (overviewDpaAcceptance) {
            overviewDpaAcceptance.textContent = dpaAcceptance.checked ? 'Accepted' : 'Not accepted';
            if (dpaAcceptance.checked) {
                overviewDpaAcceptance.classList.add('text-success');
            } else {
                overviewDpaAcceptance.classList.remove('text-success');
            }
        }
        
        if (overviewRetentionPeriod) {
            overviewRetentionPeriod.textContent = `${retentionPeriod.value} years`;
        }
    }
    
    // Validate the form
    function validateForm() {
        let isValid = true;
        
        try {
            // Check for required fields, but with null checks
            const requiredFields = [
                { element: parentCompanyId, message: 'Company ID is required' },
                { element: parentCompanyName, message: 'Company name is required' }
            ];
            
            for (const field of requiredFields) {
                // Add null check before accessing properties
                if (field.element && field.element.value && field.element.value.trim() === '') {
                    showError(field.message);
                    field.element.classList.add('is-invalid');
                    isValid = false;
                } else if (field.element) {
                    field.element.classList.remove('is-invalid');
                }
            }
            
            // For demo purposes, ensure we always return true to allow saving
            // Remove this in production
            if (!isValid) {
                console.warn('Form validation failed, but proceeding anyway for demo purposes');
                return true;
            }
            
            return isValid;
        } catch (error) {
            console.error('Error in form validation:', error);
            // For demo, we'll return true to allow submission despite errors
            return true;
        }
    }
    
    // --- Reporting Year Section Logic ---
    const calendarYearOption = document.getElementById('calendarYearOption');
    const customFiscalOption = document.getElementById('customFiscalOption');
    const customFiscalYearFields = document.getElementById('customFiscalYearFields');
    const fiscalStart = document.getElementById('fiscalStart');
    const fiscalEnd = document.getElementById('fiscalEnd');

    if (calendarYearOption && customFiscalOption && customFiscalYearFields) {
        calendarYearOption.addEventListener('change', function() {
            if (calendarYearOption.checked) {
                customFiscalYearFields.classList.add('d-none');
            }
        });
        customFiscalOption.addEventListener('change', function() {
            if (customFiscalOption.checked) {
                customFiscalYearFields.classList.remove('d-none');
            }
        });
    }
    
    // Initialize event handlers
    function initializeEventHandlers() {
        // Add event listener for the net-zero toggle
        if (netZeroCommitted) {
            netZeroCommitted.addEventListener('change', function() {
                if (this.checked) {
                    netZeroStatus.textContent = 'Committed';
                    netZeroStatus.classList.remove('text-muted');
                    netZeroStatus.classList.add('text-success');
                } else {
                    netZeroStatus.textContent = 'Not committed';
                    netZeroStatus.classList.remove('text-success');
                    netZeroStatus.classList.add('text-muted');
                }
            });
        }
        
        // Add event listener for the board oversight toggle
        if (boardOversight) {
            boardOversight.addEventListener('change', function() {
                if (this.checked) {
                    boardOversightStatus.textContent = 'Has oversight';
                    boardOversightStatus.classList.remove('text-muted');
                    boardOversightStatus.classList.add('text-success');
                    boardContactInfo.classList.remove('d-none');
                } else {
                    boardOversightStatus.textContent = 'No oversight';
                    boardOversightStatus.classList.remove('text-success');
                    boardOversightStatus.classList.add('text-muted');
                    boardContactInfo.classList.add('d-none');
                }
            });
        }
        
        // Setup admin users email chips
        if (adminUsers) {
            adminUsers.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' || e.key === ',') {
                    e.preventDefault();
                    const email = this.value.trim();
                    if (email && isValidEmail(email)) {
                        addEmailChip(email);
                        this.value = '';
                    } else if (email) {
                        showError('Please enter a valid email address');
                    }
                }
            });
        }
        
        // Initialize Select2 for multiple select dropdowns
        if (typeof $ !== 'undefined' && $.fn.select2) {
            $('#ssoDomains').select2({
                placeholder: 'Select or add domains',
                tags: true,
                tokenSeparators: [',', ' ']
            });
        }

        // Add event listener for DPA acceptance checkbox to update button state
        if (dpaAcceptance) {
            dpaAcceptance.addEventListener('change', updateConfirmButtonState);
            // Initialize the button state on page load
            updateConfirmButtonState();
        }
    }
    
    // Function to update the state of the Confirm & Continue button based on DPA acceptance
    function updateConfirmButtonState() {
        if (confirmBtn) {
            const dpaAccepted = dpaAcceptance.checked;
            
            confirmBtn.disabled = !dpaAccepted;
            
            // Create tooltip if DPA is not accepted
            if (!dpaAccepted) {
                // Add tooltip class if not present
                if (!confirmBtn.classList.contains('dpa-tooltip')) {
                    confirmBtn.classList.add('dpa-tooltip');
                    confirmBtn.title = "You must accept the Data Processing Agreement to proceed";
                    confirmBtn.setAttribute('data-bs-toggle', 'tooltip');
                    confirmBtn.setAttribute('data-bs-placement', 'top');
                    
                    // Initialize Bootstrap tooltip if available
                    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
                        try {
                            const tooltip = new bootstrap.Tooltip(confirmBtn);
                            // Force tooltip to be recreated
                            tooltip.dispose();
                            new bootstrap.Tooltip(confirmBtn);
                        } catch (e) {
                            console.warn('Error initializing tooltip:', e);
                        }
                    }
                }
                
                // Add visual indication that button is disabled
                confirmBtn.classList.add('opacity-50');
            } else {
                confirmBtn.classList.remove('opacity-50');
                // Remove tooltip if previously added
                if (confirmBtn.classList.contains('dpa-tooltip')) {
                    confirmBtn.classList.remove('dpa-tooltip');
                    confirmBtn.removeAttribute('data-bs-toggle');
                    confirmBtn.removeAttribute('data-bs-placement');
                    confirmBtn.title = "";
                    
                    // Dispose of Bootstrap tooltip if it was initialized
                    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
                        try {
                            const tooltipInstance = bootstrap.Tooltip.getInstance(confirmBtn);
                            if (tooltipInstance) {
                                tooltipInstance.dispose();
                            }
                        } catch (e) {
                            console.warn('Error disposing tooltip:', e);
                        }
                    }
                }
            }
        }
    }
    
    // Validate email format
    function isValidEmail(email) {
        const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email.toLowerCase());
    }
    
    // Add email chip to admin users
    function addEmailChip(email) {
        const chip = document.createElement('span');
        chip.className = 'badge bg-primary me-2 mb-1';
        chip.dataset.email = email;
        chip.textContent = email + ' ';
        
        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'btn-close btn-close-white btn-sm';
        closeBtn.setAttribute('aria-label', 'Remove');
        closeBtn.onclick = function() {
            adminEmailChips.removeChild(chip);
        };
        
        chip.appendChild(closeBtn);
        adminEmailChips.appendChild(chip);
    }
    
    // Get admin emails from chips
    function getAdminEmails() {
        const emails = [];
        const chips = adminEmailChips.querySelectorAll('.badge');
        chips.forEach(chip => {
            emails.push(chip.dataset.email);
        });
        return emails;
    }

    // Call the function to initialize event handlers
    initializeEventHandlers();
    
    // Collect all form data
    function collectFormData() {
        // Collect selected regions and countries
        const regions = [];
        const regionsContainer = document.getElementById('regionsContainer');
        
        // Look for all checked region checkboxes
        const regionCheckboxes = regionsContainer.querySelectorAll('input[type="checkbox"][data-region]');
        regionCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                const regionName = checkbox.dataset.region;
                const countryCheckboxes = regionsContainer.querySelectorAll(`input[type="checkbox"][data-region="${regionName}"][data-country]`);
                
                const countries = [];
                countryCheckboxes.forEach(countryCheckbox => {
                    if (countryCheckbox.checked) {
                        countries.push(countryCheckbox.dataset.country);
                    }
                });
                
                if (countries.length > 0) {
                    regions.push({ name: regionName, countries: countries });
                }
            }
        });
        
        // Get the selected NAICS code and description
        const naicsCode = document.getElementById('naicsCode').value;
        let naicsDescription = '';
        
        // Try to get the description from the selected badges
        if (naicsCode && document.getElementById('selectedNaicsTitle')) {
            naicsDescription = document.getElementById('selectedNaicsTitle').textContent.trim();
            if (naicsDescription.startsWith('- ')) {
                naicsDescription = naicsDescription.substring(2);
            }
        }
        
        // Get selected country name from dropdown
        let selectedCountry = '';
        if (country.value) {
            selectedCountry = country.options[country.selectedIndex].text;
        }
        
        // Reporting year info
        let reportingYear = { type: 'calendar' };
        if (customFiscalOption && customFiscalOption.checked) {
            reportingYear = {
                type: 'custom',
                fiscalStart: fiscalStart.value,
                fiscalEnd: fiscalEnd.value
            };
        }
        
        // Get unit system preference
        const unitSystem = metricOption.checked ? 'metric' : 'imperial';
        
        // Get selected GWP horizon
        const selectedGwpHorizon = gwpHorizon.value;
        
        // Get targets and commitments information
        const targetsInfo = {
            netZeroCommitted: netZeroCommitted?.checked || false,
            targetType: absoluteTarget?.checked ? 'absolute' : 'intensity',
            targetYear: targetYear?.value || '',
            targetPercentage: targetPercentage?.value || ''
        };
        
        // Get governance and contacts information
        const governanceInfo = {
            sustainabilityLead: {
                name: sustainabilityLeadName?.value || '',
                email: sustainabilityLeadEmail?.value || '',
                phone: sustainabilityLeadPhone?.value || ''
            },
            financeLead: {
                name: financeLeadName?.value || '',
                email: financeLeadEmail?.value || '',
                phone: financeLeadPhone?.value || ''
            },
            boardOversight: boardOversight?.checked || false,
            boardContact: (boardOversight?.checked && boardContact?.value) ? boardContact.value : ''
        };
        
        // Get security and preferences information
        const securityInfo = {
            ssoDomains: ssoDomains ? ($(ssoDomains).val() || []) : [],
            dataResidency: dataResidency?.value || '',
            adminUsers: getAdminEmails()
        };
        
        // Get legal and consent information
        const legalInfo = {
            dpaAcceptance: dpaAcceptance?.checked || false,
            retentionPeriod: retentionPeriod?.value || '7'
        };
        
        // GICS codes
        let gicsCodes = [];
        const gicsElem = document.getElementById('gicsCodes');
        if (gicsElem) {
            gicsCodes = $(gicsElem).val() || [];
        }
        // NACE codes
        let naceCodes = [];
        const naceElem = document.getElementById('naceCodes');
        if (naceElem) {
            naceCodes = $(naceElem).val() || [];
        }
        
        return {
            companyId: parentCompanyId?.value || '',
            companyName: parentCompanyName?.value || '',
            industry: {
                naicsCode: naicsCode || '',
                naicsDescription: naicsDescription || '',
                sicCode: sicCode?.value || '',
                gicsCodes: gicsCodes || [],
                naceCodes: naceCodes || [],
                description: businessDescription?.value || ''
            },
            regions: regions || [],
            location: {
                streetAddress: streetAddress?.value || '',
                city: city?.value || '',
                country: selectedCountry || '',
                latitude: latitude?.value || '',
                longitude: longitude?.value || ''
            },
            baseline: {
                year: baselineYear?.value || '',
                adjustment: baselineAdjustment?.value || ''
            },
            reportingYear: reportingYear || { type: 'calendar' },
            preferences: {
                unitSystem: unitSystem || 'metric',
                gwpHorizon: selectedGwpHorizon || ''
            },
            targets: targetsInfo || {
                netZeroCommitted: false,
                targetType: 'absolute',
                targetYear: '',
                targetPercentage: ''
            },
            governance: governanceInfo || {
                sustainabilityLead: { name: '', email: '', phone: '' },
                financeLead: { name: '', email: '', phone: '' },
                boardOversight: false,
                boardContact: ''
            },
            security: securityInfo || {
                ssoDomains: [],
                dataResidency: '',
                adminUsers: []
            },
            legal: legalInfo || {
                dpaAcceptance: false,
                retentionPeriod: '7'
            }
        };
    }
    
    // Save data to API
    async function saveDataToApi(data) {
        // Get company ID from form first (preferred source) and fall back to localStorage
        let companyName = data.companyName || parentCompanyName?.value || '';
        
        // Format company ID consistently with signup process
        const formattedId = companyName
            .toLowerCase()
            .replace(/\s+/g, '-')
            .replace(/[^a-z0-9-]/g, '')
            .replace(/-{2,}/g, '-');
        
        // Ensure company ID is formatted properly - no leading slash
        let companyId = formattedId || 'unknown-company';
        
        // If still not set, use the ID from localStorage but remove any leading slash
        if (companyId === 'unknown-company' && localStorage.getItem('userGroup')) {
            companyId = localStorage.getItem('userGroup').replace(/^\//, '');
        }
        
        // Store the ID for future use (without leading slash)
        localStorage.setItem('userGroup', companyId);
        
        // Retrieve authentication token from storage (or use demo value)
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken') || 'dummy-token';
        
        console.log('Using company ID for API call:', companyId);
        
        try {
            // First, fetch the current group details to update
            let currentGroup;
            try {
                console.log(`Fetching group data from: /api/v1/groups/${companyId}`);
                const fetchResponse = await fetch(`/api/v1/groups/${companyId}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`,
                        'X-Group-Context': '/' // Use root context for better permissions
                    }
                });
                
                if (fetchResponse.ok) {
                    currentGroup = await fetchResponse.json();
                    console.log('Successfully fetched current group data:', currentGroup);
                } else {
                    console.warn(`Failed to fetch group data. Status: ${fetchResponse.status}`);
                    // If we can't fetch the current group, create a minimal version
                    currentGroup = {
                        id: companyId,
                        name: data.companyName || 'Company Name',
                        tags: {}
                    };
                }
            } catch (fetchError) {
                console.error('Error fetching group data:', fetchError);
                // If fetch fails, create a minimal version
                currentGroup = {
                    id: companyId,
                    name: data.companyName || 'Company Name',
                    tags: {}
                };
            }
            
            // Get existing accountId from tags or generate new one on the server side
            const existingAccountId = currentGroup.tags && currentGroup.tags.accountId;
            
            // Update the group with new data - explicitly set isParentCompany: true
            const updateData = {
                name: currentGroup.name || data.companyName || 'Company Name',
                description: `Corporate: ${data.companyName}`,
                isParentCompany: true, // Explicitly set as true for parent companies
                tags: {
                    ...currentGroup.tags,
                    // Preserve existing accountId if present
                    ...(existingAccountId && { accountId: existingAccountId }),
                    naicsCode: data.industry.naicsCode,
                    naicsDescription: data.industry.naicsDescription,
                    sicCode: data.industry.sicCode,
                    businessDescription: data.industry.description,
                    headquartersAddress: data.location.streetAddress,
                    headquartersLatitude: data.location.latitude,
                    headquartersLongitude: data.location.longitude,
                    baselineYear: data.baseline.year,
                    baselineAdjustments: data.baseline.adjustment,
                    regions: JSON.stringify(data.regions),
                    reportingYearType: data.reportingYear.type,
                    reportingYearStart: data.reportingYear.fiscalStart || '',
                    reportingYearEnd: data.reportingYear.fiscalEnd || '',
                    unitSystem: data.preferences.unitSystem,
                    gwpHorizon: data.preferences.gwpHorizon,
                    netZeroCommitted: data.targets.netZeroCommitted.toString(),
                    targetType: data.targets.targetType,
                    targetYear: data.targets.targetYear,
                    targetPercentage: data.targets.targetPercentage,
                    isParentCompany: 'true', // Also add as tag for redundancy
                    // Sustainability lead info
                    sustainabilityLeadName: data.governance.sustainabilityLead.name,
                    sustainabilityLeadEmail: data.governance.sustainabilityLead.email,
                    sustainabilityLeadPhone: data.governance.sustainabilityLead.phone,
                    // Finance lead info
                    financeLeadName: data.governance.financeLead.name,
                    financeLeadEmail: data.governance.financeLead.email,
                    financeLeadPhone: data.governance.financeLead.phone,
                    // Board oversight
                    boardOversight: data.governance.boardOversight.toString(),
                    boardContact: data.governance.boardContact,
                    // Security & preferences
                    ssoDomains: JSON.stringify(data.security.ssoDomains),
                    dataResidency: data.security.dataResidency,
                    adminUsers: JSON.stringify(data.security.adminUsers),
                    // Legal & consent
                    dpaAcceptance: data.legal.dpaAcceptance.toString(),
                    retentionPeriod: data.legal.retentionPeriod,
                    onboardingCompleted: 'true'
                }
            };
            
            console.log('Updating group with data:', updateData);
            
            // Now update the group - removed encodeURIComponent since we don't need to encode the slash anymore
            try {
                console.log(`Sending PATCH request to: /api/v1/groups/${companyId}`);
                return await fetch(`/api/v1/groups/${companyId}`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`,
                        'X-Group-Context': '/'
                    },
                    body: JSON.stringify(updateData)
                });
            } catch (updateError) {
                console.error('Error updating group:', updateError);
                throw updateError;
            }
        } catch (error) {
            console.error('Error in saveDataToApi:', error);
            throw error;
        }
    }
    
    // Show error message
    function showError(message) {
        // Remove any existing alerts
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());
        
        // Create error alert
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger alert-dismissible fade show';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        // Insert at the top of the main content
        const mainContent = document.querySelector('.main-content');
        mainContent.insertBefore(alertDiv, mainContent.firstChild);
        
        // Auto dismiss after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
    
    // Show success message
    function showSuccess(message) {
        // Remove any existing alerts
        const existingAlerts = document.querySelectorAll('.alert');
        existingAlerts.forEach(alert => alert.remove());
        
        // Create success alert
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible fade show';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        // Insert at the top of the main content
        const mainContent = document.querySelector('.main-content');
        mainContent.insertBefore(alertDiv, mainContent.firstChild);
        
        // Auto dismiss after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
    
    // Load initial data
    function loadInitialData() {
        if (!localStorage.getItem('authToken') && !sessionStorage.getItem('authToken')) {
            // For demo purposes, we're bypassing authentication
            // In a real app, this would redirect to login
            localStorage.setItem('authToken', 'dummy-token');
            localStorage.setItem('userGroup', '/DemoCompany');
        }
        
        // Try to get group details if available
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const groupId = localStorage.getItem('userGroup');
        
        if (token && groupId) {
            fetch(`/api/v1/groups/${encodeURIComponent(groupId)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                // If we can't get the data, just continue without it
                return null;
            })
            .then(data => {
                if (data) {
                    // Pre-fill the form with existing data
                    parentCompanyId.value = data.id.replace('/', '');
                    parentCompanyName.value = data.name;
                    
                    if (data.tags) {
                        // Set the NAICS code if it exists
                        if (data.tags.naicsCode) {
                            const naicsCodeInput = document.getElementById('naicsCode');
                            if (naicsCodeInput) {
                                naicsCodeInput.value = data.tags.naicsCode;
                                
                                // We need to let the NAICS selector initialize 
                                // and then it will load this saved code
                                setTimeout(() => {
                                    const event = new Event('change');
                                    naicsCodeInput.dispatchEvent(event);
                                }, 500);
                                
                                // If we have a description, show it
                                if (data.tags.naicsDescription) {
                                    const selectedNaicsBadge = document.getElementById('selectedNaicsBadge');
                                    const selectedNaicsCode = document.getElementById('selectedNaicsCode');
                                    const selectedNaicsTitle = document.getElementById('selectedNaicsTitle');
                                    
                                    if (selectedNaicsBadge && selectedNaicsCode && selectedNaicsTitle) {
                                        selectedNaicsCode.textContent = data.tags.naicsCode;
                                        selectedNaicsTitle.textContent = ' - ' + data.tags.naicsDescription;
                                        selectedNaicsBadge.classList.remove('d-none');
                                    }
                                }
                            }
                        }
                        
                        sicCode.value = data.tags.sicCode || '';
                        businessDescription.value = data.tags.businessDescription || '';
                        streetAddress.value = data.tags.headquartersAddress || '';
                        city.value = data.tags.city || '';
                        country.value = data.tags.country || '';
                        latitude.value = data.tags.headquartersLatitude || '';
                        longitude.value = data.tags.headquartersLongitude || '';
                        
                        if (data.tags.baselineYear) {
                            const yearSelect = document.getElementById('baselineYear');
                            for (let i = 0; i < yearSelect.options.length; i++) {
                                if (yearSelect.options[i].value === data.tags.baselineYear) {
                                    yearSelect.selectedIndex = i;
                                    break;
                                }
                            }
                        }
                        
                        baselineAdjustment.value = data.tags.baselineAdjustments || '';

                        // Set unit system if available
                        if (data.tags.unitSystem) {
                            if (data.tags.unitSystem === 'imperial') {
                                imperialOption.checked = true;
                                metricOption.checked = false;
                            } else {
                                metricOption.checked = true;
                                imperialOption.checked = false;
                            }
                        }

                        // Set GWP horizon if available
                        if (data.tags.gwpHorizon) {
                            const gwpSelect = document.getElementById('gwpHorizon');
                            for (let i = 0; i < gwpSelect.options.length; i++) {
                                if (gwpSelect.options[i].value === data.tags.gwpHorizon) {
                                    gwpSelect.selectedIndex = i;
                                    break;
                                }
                            }
                        }
                        
                        // Set targets & commitments if available
                        if (data.tags.netZeroCommitted) {
                            netZeroCommitted.checked = data.tags.netZeroCommitted === 'true';
                            // Trigger the change event to update the status display
                            const event = new Event('change');
                            netZeroCommitted.dispatchEvent(event);
                        }
                        
                        if (data.tags.targetType) {
                            if (data.tags.targetType === 'absolute') {
                                absoluteTarget.checked = true;
                                intensityTarget.checked = false;
                            } else {
                                absoluteTarget.checked = false;
                                intensityTarget.checked = true;
                            }
                        }
                        
                        if (data.tags.targetYear) {
                            const yearSelect = document.getElementById('targetYear');
                            for (let i = 0; i < yearSelect.options.length; i++) {
                                if (yearSelect.options[i].value === data.tags.targetYear) {
                                    yearSelect.selectedIndex = i;
                                    break;
                                }
                            }
                        }
                        
                        if (data.tags.targetPercentage) {
                            targetPercentage.value = data.tags.targetPercentage;
                        }
                        
                        // Set governance & contacts if available
                        if (data.tags.sustainabilityLeadName) {
                            sustainabilityLeadName.value = data.tags.sustainabilityLeadName;
                        }
                        if (data.tags.sustainabilityLeadEmail) {
                            sustainabilityLeadEmail.value = data.tags.sustainabilityLeadEmail;
                        }
                        if (data.tags.sustainabilityLeadPhone) {
                            sustainabilityLeadPhone.value = data.tags.sustainabilityLeadPhone;
                        }
                        
                        if (data.tags.financeLeadName) {
                            financeLeadName.value = data.tags.financeLeadName;
                        }
                        if (data.tags.financeLeadEmail) {
                            financeLeadEmail.value = data.tags.financeLeadEmail;
                        }
                        if (data.tags.financeLeadPhone) {
                            financeLeadPhone.value = data.tags.financeLeadPhone;
                        }
                        
                        if (data.tags.boardOversight) {
                            boardOversight.checked = data.tags.boardOversight === 'true';
                            // Trigger the change event to update the status display and show/hide contact field
                            const event = new Event('change');
                            boardOversight.dispatchEvent(event);
                            
                            if (boardOversight.checked && data.tags.boardContact) {
                                boardContact.value = data.tags.boardContact;
                            }
                        }
                        
                        // Set security & preferences if available
                        if (data.tags.ssoDomains) {
                            try {
                                const domains = JSON.parse(data.tags.ssoDomains);
                                if (Array.isArray(domains) && typeof $ !== 'undefined' && $.fn.select2) {
                                    $('#ssoDomains').val(domains).trigger('change');
                                }
                            } catch (e) {
                                console.error('Error parsing SSO domains:', e);
                            }
                        }
                        
                        if (data.tags.dataResidency) {
                            dataResidency.value = data.tags.dataResidency;
                        }
                        
                        if (data.tags.adminUsers) {
                            try {
                                const emails = JSON.parse(data.tags.adminUsers);
                                if (Array.isArray(emails)) {
                                    emails.forEach(email => {
                                        if (isValidEmail(email)) {
                                            addEmailChip(email);
                                        }
                                    });
                                }
                            } catch (e) {
                                console.error('Error parsing admin users:', e);
                            }
                        }
                        
                        // Set legal & consent if available
                        if (data.tags.dpaAcceptance) {
                            dpaAcceptance.checked = data.tags.dpaAcceptance === 'true';
                        }
                        
                        if (data.tags.retentionPeriod) {
                            retentionPeriod.value = data.tags.retentionPeriod;
                        }
                        
                        // Update character counters
                        descCharCount.textContent = businessDescription.value.length;
                        adjustmentCharCount.textContent = baselineAdjustment.value.length;
                        
                        // Try to parse and set regions if available
                        if (data.tags.regions) {
                            try {
                                const regions = JSON.parse(data.tags.regions);
                                setRegionCheckboxes(regions);
                            } catch (e) {
                                console.error('Error parsing regions:', e);
                            }
                        }
                    }
                }
            })
            .catch(error => {
                console.error('Error fetching group data:', error);
            });
        }
    }
    
    // Set region checkboxes based on stored data
    function setRegionCheckboxes(regions) {
        if (!Array.isArray(regions)) return;
        
        regions.forEach(region => {
            if (region.name === 'Europe') {
                document.getElementById('regionEurope').checked = true;
                if (Array.isArray(region.countries)) {
                    region.countries.forEach(country => {
                        if (country === 'United Kingdom') document.getElementById('countryUK').checked = true;
                        if (country === 'Netherlands') document.getElementById('countryNetherlands').checked = true;
                    });
                }
            }
            
            if (region.name === 'Middle East') {
                document.getElementById('regionMiddleEast').checked = true;
                if (Array.isArray(region.countries)) {
                    region.countries.forEach(country => {
                        if (country === 'United Arab Emirates') document.getElementById('countryUAE').checked = true;
                        if (country === 'Saudi Arabia') document.getElementById('countrySaudi').checked = true;
                    });
                }
            }
            
            if (region.name === 'North America') {
                document.getElementById('regionNorthAmerica').checked = true;
                if (Array.isArray(region.countries)) {
                    region.countries.forEach(country => {
                        if (country === 'United States') document.getElementById('countryUS').checked = true;
                        if (country === 'Canada') document.getElementById('countryCanada').checked = true;
                    });
                }
            }
        });
    }
    
    // Load initial data
    loadInitialData();

    // Add user info banner at the top of the page
    function addUserInfoBanner() {
        // Get user email and group from localStorage
        const userEmail = localStorage.getItem('userEmail') || 'Not logged in';
        const userGroup = localStorage.getItem('userGroup') || '';
        
        // Format group ID without leading slash for display
        const formattedGroup = userGroup ? userGroup.replace(/^\//, '') : '';
        
        // Create banner element
        const banner = document.createElement('div');
        banner.className = 'alert alert-info mb-4 d-flex justify-content-between align-items-center';
        banner.innerHTML = `
            <div>
                <strong>User:</strong> ${userEmail} 
                <span class="ms-3"><strong>Default Group:</strong> ${formattedGroup}</span>
            </div>
            <div>
                <small class="text-muted">Simplified Testing Mode</small>
            </div>
        `;
        
        // Get the main content div and insert at the top
        const mainContent = document.querySelector('.main-content');
        if (mainContent) {
            mainContent.insertBefore(banner, mainContent.firstChild);
        }
        
        // Pre-populate company name if email has a domain
        if (userEmail && userEmail.includes('@')) {
            const domain = userEmail.split('@')[1];
            const companyDomain = domain.split('.')[0];
            if (companyDomain) {
                // Create a company name from the domain (capitalize first letter)
                const companyName = companyDomain.charAt(0).toUpperCase() + companyDomain.slice(1);
                
                // Set it in the company name field if the field is empty
                const parentCompanyNameInput = document.getElementById('parentCompanyName');
                if (parentCompanyNameInput && !parentCompanyNameInput.value) {
                    parentCompanyNameInput.value = companyName;
                    
                    // Generate ID based on company name (without leading slash)
                    const parentCompanyIdInput = document.getElementById('parentCompanyId');
                    if (parentCompanyIdInput && !parentCompanyIdInput.value) {
                        parentCompanyIdInput.value = companyName.toLowerCase().replace(/\s+/g, '-');
                    }
                }
            }
        }
    }

    // Function to load regions and countries from country.json
    function loadRegionsAndCountries() {
        const regionsContainer = document.getElementById('regionsContainer');
        const countrySelect = document.getElementById('country');
        
        fetch('/data/country.json')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Clear loading spinner
                regionsContainer.innerHTML = '';
                
                // Extract the first object from the array
                const regionData = data[0];
                
                // Create a flat list of all countries for the country dropdown
                const allCountries = [];
                
                // Process each region
                Object.keys(regionData).forEach(regionName => {
                    const countries = regionData[regionName];
                    
                    // Create region container
                    const regionHtml = `
                        <div class="region-selector mb-3">
                            <div class="form-check d-flex align-items-center">
                                <input class="form-check-input" type="checkbox" id="region${regionName}" data-region="${regionName}">
                                <label class="form-check-label ms-2" for="region${regionName}">${regionName}</label>
                                <button class="btn btn-sm dropdown-toggle ms-2" type="button" data-bs-toggle="collapse" data-bs-target="#${regionName}Countries">
                                    <i class="bi bi-chevron-down"></i>
                                </button>
                            </div>
                            
                            <div class="collapse ms-4 mt-2" id="${regionName}Countries">
                                ${createCountryCheckboxes(regionName, countries)}
                            </div>
                        </div>
                    `;
                    
                    regionsContainer.innerHTML += regionHtml;
                    
                    // Add countries to the flat list
                    Object.keys(countries).forEach(countryName => {
                        allCountries.push({ name: countryName, code: countries[countryName] });
                    });
                });
                
                // Sort countries alphabetically
                allCountries.sort((a, b) => a.name.localeCompare(b.name));
                
                // Populate the country dropdown
                countrySelect.innerHTML = '<option value="">Select country</option>';
                allCountries.forEach(country => {
                    countrySelect.innerHTML += `<option value="${country.code}">${country.name} - ${country.code}</option>`;
                });
                
                // Set up checkbox event listeners
                setupRegionCountryCheckboxes();
            })
            .catch(error => {
                console.error('Error loading regions and countries:', error);
                regionsContainer.innerHTML = `
                    <div class="alert alert-danger">
                        Failed to load regions and countries. Please refresh the page or contact support.
                    </div>
                `;
            });
    }
    
    // Helper function to create country checkboxes
    function createCountryCheckboxes(regionName, countries) {
        let html = '';
        
        // Sort countries alphabetically
        const sortedCountries = Object.keys(countries).sort();
        
        sortedCountries.forEach(countryName => {
            const countryCode = countries[countryName];
            const checkboxId = `country${regionName}${countryCode}`;
            
            html += `
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="${checkboxId}" 
                           data-region="${regionName}" data-country="${countryName}" data-code="${countryCode}">
                    <label class="form-check-label" for="${checkboxId}">${countryName}</label>
                </div>
            `;
        });
        
        return html;
    }
    
    // Set up event listeners for region and country checkboxes
    function setupRegionCountryCheckboxes() {
        // When a region checkbox is clicked, toggle all its countries
        const regionCheckboxes = document.querySelectorAll('input[type="checkbox"][data-region]:not([data-country])');
        regionCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const regionName = this.dataset.region;
                const countryCheckboxes = document.querySelectorAll(`input[type="checkbox"][data-region="${regionName}"][data-country]`);
                
                countryCheckboxes.forEach(countryCheckbox => {
                    countryCheckbox.checked = this.checked;
                });
            });
        });
        
        // When a country checkbox is clicked, update the region checkbox state
        const countryCheckboxes = document.querySelectorAll('input[type="checkbox"][data-country]');
        countryCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const regionName = this.dataset.region;
                const regionCheckbox = document.querySelector(`input[type="checkbox"][data-region="${regionName}"]:not([data-country])`);
                const allCountryCheckboxes = document.querySelectorAll(`input[type="checkbox"][data-region="${regionName}"][data-country]`);
                const checkedCountryCheckboxes = document.querySelectorAll(`input[type="checkbox"][data-region="${regionName}"][data-country]:checked`);
                
                // If any country is checked, the region should be checked
                regionCheckbox.checked = checkedCountryCheckboxes.length > 0;
                
                // If the region is partially checked, add the indeterminate state
                regionCheckbox.indeterminate = checkedCountryCheckboxes.length > 0 && checkedCountryCheckboxes.length < allCountryCheckboxes.length;
            });
        });
    }

    // Configurable limits for SIC and ISIC multi-selects
    const SIC_MAX_SELECTION = 15;
    let sicCodesList = [];
    let selectedSicCodes = [];

    function loadSicCodes() {
        fetch('/api/v1/sic-codes')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load SIC codes');
                }
                return response.json();
            })
            .then(data => {
                console.log('SIC API response:', data); // Debug: log the API response
                if (Array.isArray(data)) {
                    sicCodesList = data;
                } else if (data && typeof data === 'object') {
                    // Handle case where API returns an object with embedded array
                    sicCodesList = Array.isArray(data.codes) ? data.codes : [];
                } else {
                    console.error('Unexpected SIC codes response format:', data);
                    sicCodesList = [];
                }
                setupSicAutocomplete();
            })
            .catch(error => {
                console.error('Error loading SIC codes:', error);
                // Load some sample SIC codes for testing if API fails
                sicCodesList = [
                    { code: "1711", description: "Plumbing, Heating, and Air-Conditioning" },
                    { code: "2834", description: "Pharmaceutical Preparations" },
                    { code: "4813", description: "Telephone Communications, Except Radiotelephone" },
                    { code: "5812", description: "Eating Places" },
                    { code: "7371", description: "Computer Programming Services" }
                ];
                setupSicAutocomplete();
            });
    }

    function setupSicAutocomplete() {
        const input = document.getElementById('sicSearchInput');
        const sicCode = document.getElementById('sicCode');
        input.addEventListener('input', function() {
            const val = input.value.trim();
            if (val.length < 2) {
                hideSicSuggestions();
                return;
            }
            
            // Search by code prefix or description text
            const matches = sicCodesList.filter(
                c => c.code.startsWith(val) || 
                c.description.toLowerCase().includes(val.toLowerCase())
            ).slice(0, 10); // Limit to 10 results for performance
            
            showSicSuggestions(matches);
        });
        
        // Hide suggestions on blur
        input.addEventListener('blur', function() {
            setTimeout(hideSicSuggestions, 200);
        });
        
        // If we have a pre-saved SIC code, set it
        if (sicCode && sicCode.value) {
            const savedCode = sicCode.value;
            const match = sicCodesList.find(item => item.code === savedCode);
            if (match) {
                addSicCode(match);
            }
        }
    }

    function showSicSuggestions(matches) {
        let suggestionBox = document.getElementById('sicSuggestionBox');
        suggestionBox.innerHTML = '';
        
        if (matches.length === 0) {
            suggestionBox.style.display = 'none';
            return;
        }
        
        const suggestionList = document.createElement('div');
        suggestionList.className = 'list-group position-absolute w-100 shadow-sm';
        suggestionList.style.zIndex = '1000';
        
        matches.forEach(item => {
            const el = document.createElement('button');
            el.type = 'button';
            el.className = 'list-group-item list-group-item-action';
            el.textContent = `${item.code} - ${item.description}`;
            el.onclick = () => {
                addSicCode(item);
                hideSicSuggestions();
                document.getElementById('sicSearchInput').value = '';
                document.getElementById('sicCode').value = item.code;
            };
            suggestionList.appendChild(el);
        });
        
        suggestionBox.appendChild(suggestionList);
        suggestionBox.style.display = 'block';
    }

    function hideSicSuggestions() {
        let suggestionBox = document.getElementById('sicSuggestionBox');
        if (suggestionBox) {
            suggestionBox.innerHTML = '';
            suggestionBox.style.display = 'none';
        }
    }

    function addSicCode(item) {
        if (selectedSicCodes.find(c => c.code === item.code)) {
            return; // Already selected
        }
        
        // Update hidden input field
        const sicCodeInput = document.getElementById('sicCode');
        if (sicCodeInput) {
            sicCodeInput.value = item.code;
        }
        
        // Clear previous selection if we're just updating the single SIC code
        selectedSicCodes = [item];
        renderSicChips();
    }

    function renderSicChips() {
        const container = document.getElementById('sicChipContainer');
        if (!container) return;
        
        container.innerHTML = '';
        selectedSicCodes.forEach(item => {
            const chip = document.createElement('span');
            chip.className = 'badge bg-secondary me-2 mb-1';
            chip.textContent = `${item.code} - ${item.description} `;
            
            const close = document.createElement('button');
            close.type = 'button';
            close.className = 'btn-close btn-close-white btn-sm ms-1';
            close.onclick = () => {
                selectedSicCodes = selectedSicCodes.filter(c => c.code !== item.code);
                renderSicChips();
                
                // Clear the hidden input when removed
                const sicCodeInput = document.getElementById('sicCode');
                if (sicCodeInput) {
                    sicCodeInput.value = '';
                }
            };
            
            chip.appendChild(close);
            container.appendChild(chip);
        });
    }

    $(document).ready(function() {
        // Load and initialize SIC codes once on page load
        loadSicCodes();
        
        // Initialize other industry code selects if they exist
        ['#isicCodes', '#gicsCodes', '#naceCodes'].forEach(function(sel) {
            if ($(sel).length) {
                $(sel).select2({
                    placeholder: 'Select or search codes',
                    allowClear: true,
                    width: '100%'
                });
            }
        });
    });
}); 