"use client";

import React, { useState } from 'react';
import Styles from '@/app/styles/AttractionComponent.module.css';

const Attraction = () => {
    const [attractionData, setAttractionData] = useState({
        name: '',
        intro: '',
        bestTime: '',
        specialReasons: [''],  
        mustDo: [''],          
        photoSpot: '',
        tips: [''],            
        recommendations: [''],
        summary: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setAttractionData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleListChange = (e, index, field) => {
        const newArray = [...attractionData[field]];
        newArray[index] = e.target.value;
        setAttractionData(prevState => ({
            ...prevState,
            [field]: newArray
        }));
    };

    const addField = (field) => {
        setAttractionData(prevState => {
            const updatedArray = [...prevState[field]];
            if (field === 'specialReasons' && updatedArray.length < 3) {
                updatedArray.push('');
            } else if (field !== 'specialReasons' && updatedArray.length < 3) {
                updatedArray.push('');
            }
            return {
                ...prevState,
                [field]: updatedArray
            };
        });
    };

    const removeField = (field, index) => {
        setAttractionData(prevState => {
            const updatedArray = [...prevState[field]];
            if (updatedArray.length > 1) {
                updatedArray.splice(index, 1);
            }
            return {
                ...prevState,
                [field]: updatedArray
            };
        });
    };

    return (
        <div className={Styles.container}>
            <form className={Styles.form}>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <label htmlFor="name">명소 이름</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={attractionData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className={Styles.formGroup}>
                        <label htmlFor="intro">한 줄 소개</label>
                        <input
                            type="text"
                            id="intro"
                            name="intro"
                            value={attractionData.intro}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="bestTime">최적의 방문 시기</label>
                    <input
                        type="text"
                        id="bestTime"
                        name="bestTime"
                        placeholder='계절, 월, 또는 시간대'
                        value={attractionData.bestTime}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label>이곳이 특별한 이유</label>
                    {attractionData.specialReasons.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`주요 특징 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'specialReasons')}
                                required
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('specialReasons')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.specialReasons.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('specialReasons', attractionData.specialReasons.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>꼭 해봐야 할 것들</label>
                    {attractionData.mustDo.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`활동 또는 경험 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'mustDo')}
                                required
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('mustDo')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.mustDo.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('mustDo', attractionData.mustDo.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>알아두면 좋은 팁</label>
                    {attractionData.tips.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`유용한 정보 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'tips')}
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('tips')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.tips.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('tips', attractionData.tips.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>이런 분들에게 추천해요</label>
                    {attractionData.recommendations.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`추천 대상 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'recommendations')}
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('recommendations')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.recommendations.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('recommendations', attractionData.recommendations.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="photoSpot">인생샷 스팟</label>
                    <input
                        type="text"
                        id="photoSpot"
                        name="photoSpot"
                        value={attractionData.photoSpot}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="summary">🗣 한마디로 표현하자면</label>
                    <input
                        type="text"
                        id="summary"
                        name="summary"
                        placeholder='이 장소를 함축적으로 표현하는 짧은 문구'
                        value={attractionData.summary}
                        onChange={handleChange}
                        required
                    />
                </div>
            </form>
        </div>
    );
};

export default Attraction;
