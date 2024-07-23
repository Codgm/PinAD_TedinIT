"use client"
import React, { useState } from 'react';

const NavBar = () => {
    
    const Menus = [
        {name: "Home", icon: 
            (<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" class="size-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="m2.25 12 8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25" />
            </svg>)
            , dis: "-translate-x-32"},
        {name: "Search", icon: 
            (<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" class="size-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
            </svg>)
          , dis: "-translate-x-16"},
        {name: "Add", icon: 
            (<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" class="size-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>)
          , dis: "translate-x-0"},
        {name: "Hot", icon: 
            (<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" class="size-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.362 5.214A8.252 8.252 0 0 1 12 21 8.25 8.25 0 0 1 6.038 7.047 8.287 8.287 0 0 0 9 9.601a8.983 8.983 0 0 1 3.361-6.867 8.21 8.21 0 0 0 3 2.48Z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 18a3.75 3.75 0 0 0 .495-7.468 5.99 5.99 0 0 0-1.925 3.547 5.975 5.975 0 0 1-2.133-1.001A3.75 3.75 0 0 0 12 18Z" />
            </svg>)
          , dis: "translate-x-16"},
        {name: "profile", icon: 
            (<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" class="size-6">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
            </svg>)
          , dis: "translate-x-32"},
    ];
    const [active, setActive] = useState(0);
    return (
        <div className='bg-white max-h-[4.4rem] rounded-t-xl z-30 justify-between'>
            <ul className='flex w-auto justify-center relative'>
                <span className={`bg-purple-500 duration-500 ${Menus[active].dis} border-2 border-gray-900 h-16 w-16 absolute 
                -top-5 rounded-full transition-transform items-center`}>
                </span>
                {Menus.map((menu, i) => (
                    <li key={i} className='w-16'>
                        <div className=' items-center flex flex-col text-center pt-6' 
                        onClick={() => setActive(i)}
                        >
                            <span className={`text-xl cursor-pointer duration-500 z-30 ${i === active && "-mt-6 text-white"}`}>
                                {menu.icon}
                            </span>
                            <span className={`${active === i ? 'translate-y-4 duration-700 opacity-100' 
                                : 'opacity-0 translate-y-10'}`}
                                >
                                {menu.name}
                            </span>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}
export default NavBar