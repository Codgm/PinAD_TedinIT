"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from '@/app/styles/SearchPage.module.css';
import MapList from '../components/maplist';
import Layout from '../components/layout/layout';

const mockData = [
  { id: 1, name: 'John Doe', businessName: 'Doe Industries', interest: '여행메모', lat: 37.7749, lng: -122.4194 },
  { id: 2, name: 'Jane Smith', businessName: 'Smith LLC', interest: '유통', lat: 34.0522, lng: -118.2437 },
  { id: 3, name: 'Alice Johnson', businessName: 'Johnson Co.', interest: '명소', lat: 40.7128, lng: -74.0060 },
];

export default function SearchPage() {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [selectedLocation, setSelectedLocation] = useState(null);
    
    const handleChange = (e) => {
        setQuery(e.target.value);
    };

    const handleSearch = () => {
        const filteredResults = mockData.filter(item =>
            item.id.toString().includes(query) ||
            item.name.toLowerCase().includes(query.toLowerCase()) ||
            item.businessName.toLowerCase().includes(query.toLowerCase()) ||
            item.interest.toLowerCase().includes(query.toLowerCase())
        );
        setResults(filteredResults);
    };

    const handleClickResult = (location) => {
        setSelectedLocation(location);
    };

    return (
            <div className={styles.pageWrapper}>
                <div className={styles.container}>
                    <div className={styles.header}>
                        <h1>Search</h1>
                        <div className={styles.icons}>
                            <input
                                type="text"
                                value={query}
                                onChange={handleChange}
                                placeholder="Search by ID, Name, Business Name, Interest"
                                className={styles.searchInput}
                            />
                            <button
                                type="button"
                                onClick={handleSearch}
                                className={styles.searchButton}
                            >
                                Search
                            </button>
                        </div>
                    </div>
                    <div className={styles.resultsList}>
                        {results.length > 0 ? (
                            results.map(result => (
                                <div
                                    key={result.id}
                                    className={styles.resultItem}
                                    onClick={() => handleClickResult(result)}
                                >
                                    {result.name} - {result.businessName} - {result.interest}
                                </div>
                            ))
                        ) : (
                            <p className={styles.noResults}>No results found</p>
                        )}
                    </div>
                    <div className={styles.mapContainer}>
                        <MapList pins={results} showAllPins={true}/>
                    </div>
                </div>
            </div>
    );
}
