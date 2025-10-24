/**
 * Mapbox Geocoding Integration for Carbon Tracker
 * Provides address geocoding functionality using Mapbox API
 */

class MapboxGeocoding {
    constructor(apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = 'https://api.mapbox.com/geocoding/v5/mapbox.places/';
    }

    /**
     * Geocode an address using Mapbox API
     * @param {string} address - The address to geocode
     * @param {Object} options - Additional options
     * @returns {Promise<Object>} - Promise resolving to geocoding result
     */
    async geocodeAddress(address, options = {}) {
        const {
            country = null,
            proximity = null,
            bbox = null,
            limit = 1
        } = options;

        try {
            // Debug logging
            console.log('MapboxGeocoding: Starting geocode for address:', address);
            console.log('MapboxGeocoding: Options:', options);
            
            // Encode the address for URL
            const encodedAddress = encodeURIComponent(address);
            
            // Build query parameters
            const params = new URLSearchParams({
                access_token: this.apiKey,
                limit: limit.toString(),
                types: 'address,place,poi'
            });
            
            console.log('MapboxGeocoding: API Key (first 10 chars):', this.apiKey.substring(0, 10) + '...');

            // Add optional parameters
            if (country) {
                params.append('country', country);
            }
            if (proximity) {
                params.append('proximity', `${proximity.longitude},${proximity.latitude}`);
            }
            if (bbox) {
                params.append('bbox', `${bbox.minLng},${bbox.minLat},${bbox.maxLng},${bbox.maxLat}`);
            }

            const url = `${this.baseUrl}${encodedAddress}.json?${params.toString()}`;
            
            console.log('MapboxGeocoding: Full URL:', url);
            
            const response = await fetch(url);
            
            console.log('MapboxGeocoding: Response status:', response.status);
            console.log('MapboxGeocoding: Response headers:', response.headers);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('MapboxGeocoding: Error response:', errorText);
                throw new Error(`Mapbox API error: ${response.status} ${response.statusText} - ${errorText}`);
            }

            const data = await response.json();
            
            if (!data.features || data.features.length === 0) {
                throw new Error('No results found for the given address');
            }

            // Return the first (best) result, but also include all results for potential selection
            const feature = data.features[0];
            const [longitude, latitude] = feature.center;
            
            return {
                success: true,
                latitude: latitude,
                longitude: longitude,
                formatted_address: feature.place_name,
                confidence: this.calculateConfidence(feature),
                components: this.parseAddressComponents(feature),
                raw: feature,
                all_results: data.features.map(f => ({
                    latitude: f.center[1],
                    longitude: f.center[0],
                    formatted_address: f.place_name,
                    confidence: this.calculateConfidence(f),
                    place_type: f.place_type[0],
                    relevance: f.relevance
                }))
            };

        } catch (error) {
            console.error('Mapbox geocoding error:', error);
            return {
                success: false,
                error: error.message,
                latitude: null,
                longitude: null
            };
        }
    }

    /**
     * Reverse geocode coordinates to get address
     * @param {number} latitude - Latitude coordinate
     * @param {number} longitude - Longitude coordinate
     * @returns {Promise<Object>} - Promise resolving to reverse geocoding result
     */
    async reverseGeocode(latitude, longitude) {
        try {
            const params = new URLSearchParams({
                access_token: this.apiKey,
                types: 'address,place'
            });

            const url = `${this.baseUrl}${longitude},${latitude}.json?${params.toString()}`;
            
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`Mapbox API error: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            
            if (!data.features || data.features.length === 0) {
                throw new Error('No address found for the given coordinates');
            }

            const feature = data.features[0];
            
            return {
                success: true,
                formatted_address: feature.place_name,
                components: this.parseAddressComponents(feature),
                raw: feature
            };

        } catch (error) {
            console.error('Mapbox reverse geocoding error:', error);
            return {
                success: false,
                error: error.message,
                formatted_address: null
            };
        }
    }

    /**
     * Calculate confidence score based on Mapbox response
     * @param {Object} feature - Mapbox feature object
     * @returns {number} - Confidence score between 0 and 1
     */
    calculateConfidence(feature) {
        // Mapbox doesn't provide explicit confidence scores
        // We'll calculate based on place type and relevance
        const placeType = feature.place_type[0];
        const relevance = feature.relevance || 0.5;
        
        let typeScore = 0.5;
        switch (placeType) {
            case 'address':
                typeScore = 1.0;
                break;
            case 'place':
                typeScore = 0.8;
                break;
            case 'poi':
                typeScore = 0.7;
                break;
            case 'locality':
                typeScore = 0.6;
                break;
            default:
                typeScore = 0.5;
        }
        
        return Math.min(1.0, (relevance + typeScore) / 2);
    }

    /**
     * Parse address components from Mapbox feature
     * @param {Object} feature - Mapbox feature object
     * @returns {Object} - Parsed address components
     */
    parseAddressComponents(feature) {
        const components = {
            street_number: null,
            street_name: null,
            city: null,
            state: null,
            country: null,
            postal_code: null
        };

        // Parse from context array
        if (feature.context) {
            feature.context.forEach(ctx => {
                const id = ctx.id.split('.')[0];
                switch (id) {
                    case 'postcode':
                        components.postal_code = ctx.text;
                        break;
                    case 'place':
                        components.city = ctx.text;
                        break;
                    case 'region':
                        components.state = ctx.text;
                        break;
                    case 'country':
                        components.country = ctx.text;
                        break;
                }
            });
        }

        // Parse street information from properties
        if (feature.properties) {
            if (feature.properties.address) {
                components.street_number = feature.properties.address;
            }
        }

        // Parse street name from text
        if (feature.text && !components.street_name) {
            components.street_name = feature.text;
        }

        return components;
    }

    /**
     * Validate coordinates
     * @param {number} latitude - Latitude to validate
     * @param {number} longitude - Longitude to validate
     * @returns {boolean} - True if coordinates are valid
     */
    static validateCoordinates(latitude, longitude) {
        const lat = parseFloat(latitude);
        const lng = parseFloat(longitude);
        
        return !isNaN(lat) && !isNaN(lng) && 
               lat >= -90 && lat <= 90 && 
               lng >= -180 && lng <= 180;
    }

    /**
     * Format coordinates for display
     * @param {number} latitude - Latitude coordinate
     * @param {number} longitude - Longitude coordinate
     * @param {number} precision - Number of decimal places (default: 6)
     * @returns {string} - Formatted coordinate string
     */
    static formatCoordinates(latitude, longitude, precision = 6) {
        const lat = parseFloat(latitude).toFixed(precision);
        const lng = parseFloat(longitude).toFixed(precision);
        const latDir = latitude >= 0 ? 'N' : 'S';
        const lngDir = longitude >= 0 ? 'E' : 'W';
        
        return `${Math.abs(lat)}° ${latDir}, ${Math.abs(lng)}° ${lngDir}`;
    }
}

// Export for use in other scripts
window.MapboxGeocoding = MapboxGeocoding;
