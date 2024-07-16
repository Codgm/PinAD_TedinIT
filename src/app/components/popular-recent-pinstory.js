import React, { useState } from 'react';
import styles from '@/app/styles/Recommendations.module.css';
import PopularPinStory from './popularpinstory';
import RecentPinStory from './recentpinstory';

const PopularRecentPinStory = () => {
    const [activeTab, setActiveTab] = useState('popular');

    const handleTabClick = (tab) => {
        setActiveTab(tab);
    };

    return (
        <div className='h-full flex flex-grow overflow-hidden bg-purple-300 rounded-xl shadow-lg px-4'>
            <div dir='ltr' data-orientation="horizontal">
                <div className={`${styles.recommendation}`} role="tablist" aria-orientation='horizontal' tabIndex={0} data-orientation="horizontal">
                    <button
                        type='button'
                        role='tab'
                        aria-selected={activeTab === 'popular'}
                        aria-controls='radix-:r0:-content-popular'
                        data-state={activeTab === 'popular' ? 'active' : 'inactive'}
                        id='radix-:r0:-trigger-popular'
                        className={`ring-offset-background focus-visible:ring-ring ${activeTab === 'popular' ? 'bg-background text-foreground shadow' : ''} inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50`}
                        tabIndex={0}
                        data-orientation="horizontal"
                        data-radix-collection-item
                        onClick={() => handleTabClick('popular')}
                    >
                        인기 핀스토리
                    </button>
                    <button
                        type='button'
                        role='tab'
                        aria-selected={activeTab === 'recent'}
                        aria-controls='radix-:r0:-content-recent'
                        data-state={activeTab === 'recent' ? 'active' : 'inactive'}
                        id='radix-:r0:-trigger-recent'
                        className={`ring-offset-background focus-visible:ring-ring ${activeTab === 'recent' ? 'bg-background text-foreground shadow' : ''} inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50`}
                        tabIndex={0}
                        data-orientation="horizontal"
                        data-radix-collection-item
                        onClick={() => handleTabClick('recent')}
                    >
                        이전 읽은 핀스토리
                    </button>
                </div>
                <div
                    data-state={activeTab === 'popular' ? 'active' : 'inactive'}
                    data-orientation="horizontal"
                    role='tabpanel'
                    aria-labelledby='radix-:r0:-trigger-popular'
                    id='radix-:r0:-content-popular'
                    tabIndex={0}
                    className={`ring-offset-background focus-visible:ring-ring mt-2 max-h-[400px] overflow-y-auto focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ${activeTab === 'popular' ? 'block' : 'hidden'}`}
                >
                    <div className='space-y-4'>
                        <PopularPinStory />
                    </div>
                </div>
                <div
                    data-state={activeTab === 'recent' ? 'active' : 'inactive'}
                    data-orientation="horizontal"
                    role='tabpanel'
                    aria-labelledby='radix-:r0:-trigger-recent'
                    id='radix-:r0:-content-recent'
                    tabIndex={0}
                    className={`ring-offset-background focus-visible:ring-ring mt-2 max-h-[400px] overflow-y-auto focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 ${activeTab === 'recent' ? 'block' : 'hidden'}`}
                >
                    <div className='space-y-4'>
                        <RecentPinStory />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PopularRecentPinStory;
