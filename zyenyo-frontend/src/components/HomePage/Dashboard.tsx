"use client"

import { StarIcon, TrophyIcon, CommandLineIcon, ChatBubbleLeftEllipsisIcon } from "@heroicons/react/24/outline";

<CommandLineIcon className="h-6 w-6 text-gray-500" />

import Link from "next/link";

<StarIcon className="h-6 w-6 text-gray-500" />

import {useEffect, useState} from "react"
import BotStats from "./BotStats";

const Dashboard = () => {

    return (
        <>
            <div className='flex flex-row gap-0 text-6xl md:text-9xl mb-12'>
                <div className='text-white underline underline-offset-8'>Zyenyo</div><div className='text-[#F51A1F]'>Bot</div>
            </div>
            <BotStats className="my-5 hidden md:block" />
            <div className="grid grid-cols-1 md:grid-cols-2 mt-auto mb-auto gap-3">

                <Link className="flex flex-col justify-evenly hover:bg-zinc-800 ease-in p-6 gap-3 max-w-[350px] h-[180px] text-left border-2 border-white rounded-3xl " href="https://discord.com/api/oauth2/authorize?client_id=696614233944752130&permissions=137439283200&scope=bot" target="_blank">
                    <StarIcon className="h-8 w-8 text-[#F51A1F]" />
                    <div>
                        <p className="text-lg mb-1 font-bold">Get Zyenyo</p>
                        <p className="text-sm text-gray-50">Add Zyenyo to your server.</p>
                    </div>
                </Link>

                <Link className="flex flex-col justify-evenly hover:bg-zinc-800 ease-in p-6 gap-3 max-w-[350px] h-[180px] text-left border-2 border-white rounded-3xl " href="/" >
                    <CommandLineIcon className="h-8 w-8 text-[#F51A1F]" />
                    <div>
                        <p className="text-lg mb-1 font-bold">Commands</p>
                        <p className="text-sm text-gray-50">Documentation for every command Zyenyo has to offer. Coming soon!</p>
                    </div>
                </Link>

                <Link className="flex flex-col justify-evenly hover:bg-zinc-800 ease-in p-6 gap-3 max-w-[350px] h-[180px] text-left border-2 border-white rounded-3xl " href="/" >
                    <TrophyIcon className=" h-8 w-8 text-[#F51A1F]" />
                    <div>
                        <p className="text-lg mb-1 font-bold">Leaderboards</p>
                        <p className="text-sm text-gray-50">Player rankings on various typing statistics. Coming soon!</p>
                    </div>
                </Link>

                <Link className="flex flex-col justify-evenly hover:bg-zinc-800 ease-in p-6 gap-3 max-w-[350px] h-[180px] text-left border-2 border-white rounded-3xl " href="/prompts" >
                    <ChatBubbleLeftEllipsisIcon className=" h-8 w-8 text-[#F51A1F]" />
                    <div>
                        <p className="text-lg mb-1 font-bold">Prompts</p>
                        <p className="text-sm text-gray-50">Browse through the entire catalog of prompts that you can type with Zyenyo.</p>
                    </div>
                </Link>

            </div>
        </>

    )
}

export default Dashboard
