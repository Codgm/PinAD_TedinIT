"use client";
import React, { useState } from 'react';
import Styles from '@/app/styles/ReviewComponent.module.css'; 

const Review = () => {
    const [reviewData, setReviewData] = useState({
        title: '',
        location: '',
        details: {
            date: '',
            review_type: '',
            rating: '',
            strengths: '',
            weaknesses: '',
            memorable_points: '',
            recomment_actions: '',
            revisit: '',
            additional_comments: '',
        }
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setReviewData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    return (
        <div className={Styles.container}>
            <form className={Styles.form}>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            placeholder='업소 이름'
                            className={Styles.input}
                            value={reviewData.title}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="location"
                            name="location"
                            placeholder='업소 위치'
                            value={reviewData.location}
                            className={Styles.input}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <select
                            id="type"
                            name="type"
                            className={Styles.select}
                            value={reviewData.review_type}
                            onChange={handleChange}
                            required
                        >
                            <option value="">업소종류</option>
                            <option value="식당">식당</option>
                            <option value="카페">카페</option>
                            <option value="관광지">관광지</option>
                            <option value="숙소">숙소</option>
                        </select>
                    </div>
                    <div className={Styles.formGroup}>
                        <input
                            type="date"
                            id="date"
                            name="date"
                            placeholder='방문 날짜'
                            className={Styles.input}
                            value={reviewData.date}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>
                <div className={Styles.formGroup}>
                    <div className={Styles.formGroup}>
                        <input
                            type="number"
                            id="rating"
                            name="rating"
                            placeholder='평점: 1~5점까지'
                            min="1"
                            max="5"
                            value={reviewData.rating}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="strengths"
                        name="strengths"
                        placeholder='장점'
                        value={reviewData.strengths}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="weaknesses"
                        name="weaknesses"
                        placeholder='단점'
                        value={reviewData.weaknesses}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="memorablePoints"
                        name="memorablePoints"
                        placeholder='가장 기억에 남는 특징이나 경험'
                        value={reviewData.memorable_points}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="recommendations"
                        name="recommendations"
                        placeholder='추천 대상'
                        value={reviewData.recomment_actions}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="revisit">재방문 의사</label>
                    <select
                        id="revisit"
                        name="revisit"
                        className={Styles.select}
                        value={reviewData.revisit}
                        onChange={handleChange}
                        required
                    >
                        <option value="">선택하세요</option>
                        <option value="예">예</option>
                        <option value="아니오">아니오</option>
                    </select>
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="additionalComments">추가 코멘트</label>
                    <textarea
                        id="additionalComments"
                        name="additionalComments"
                        value={reviewData.additional_comments}
                        onChange={handleChange}
                    />
                </div>
            </form>
        </div>
    );
};

export default Review;
